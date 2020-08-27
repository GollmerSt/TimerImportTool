// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.misc.Constants;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.provider.Provider;

public class ProviderAssignment extends MyTabPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = -646786132995736132L;

	class ChannelAssignment {
		private int listIndex = -1;
		private final Channel channel;

		ChannelAssignment(Channel channel, int ix) {
			this.channel = channel;
			this.listIndex = ix;
		}

		ChannelAssignment(int pid, String name, String id, int ix) {
			this.channel = Provider.getProvider(pid).createChannel(name, id);
			this.listIndex = ix;
		}

		ChannelAssignment(int pid, String name, int ix) {
			this(pid, name, "", ix);
		}

		@Override
		public String toString() {
			return this.channel.getName();
		};

		public Channel getChannel() {
			return this.channel;
		};

		public int getIndex() {
			return this.listIndex;
		};

		public void setIndex(int ix) {
			this.listIndex = ix;
		};
	}

	private final ArrayList<TreeMap<String, ChannelAssignment>> providerMaps = new ArrayList<TreeMap<String, ChannelAssignment>>();
	private String lastSelectedChannel = null;
	private final String[] columnNames;

	private final JCheckBox unlockBox = new JCheckBox();
	private final JComboBox<Provider> providerCombo = new JComboBox<>();
	private final JComboBox<String> channelCombo = new JComboBox<>();
	private final JButton addChannelButton = new JButton();
	private final JButton modifyChannelButton = new JButton();
	private final JButton importButton = new JButton();
	private final JTable table;
	private TableRowSorter<TableModel> sorter = null;

	private final JLabel messageLabel = new JLabel();

	private void enableByProvider(boolean enable) {
		Provider p = (Provider) this.providerCombo.getSelectedItem();

		this.modifyChannelButton.setEnabled(p.canModify() && enable);
		this.addChannelButton.setEnabled(p.canAddChannel() && enable);
		this.importButton.setEnabled(p.canImport() && enable);
	}

	public class LockBoxChanged implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ProviderAssignment.this.messageLabel.setText("");
			ProviderAssignment.this.importButton.setText(ResourceManager.msg("IMPORT_TV"));
			boolean enable = ProviderAssignment.this.unlockBox.isSelected();
			ProviderAssignment.this.providerCombo.setEnabled(enable);
			ProviderAssignment.this.channelCombo.setEnabled(enable);
			ProviderAssignment.this.channelCombo.setEditable(enable);
			enableByProvider(enable);
			updateTable();
			ProviderAssignment.this.table.setEnabled(enable);
		}
	}

	public class ProviderSelected implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ProviderAssignment.this.messageLabel.setText("");
			ProviderAssignment.this.importButton.setText(ResourceManager.msg("IMPORT_TV"));

			enableByProvider(true);

			updateChannelComboBox();

			Provider p = (Provider) ProviderAssignment.this.providerCombo.getSelectedItem();
			if (p == null)
				return;

			int row = ProviderAssignment.this.table.getSelectedRow();
			if (row < 0)
				return;
			int selectedLine = ProviderAssignment.this.sorter.convertRowIndexToModel(row);
			MyTableObject o = (MyTableObject) ProviderAssignment.this.table.getModel().getValueAt(selectedLine,
					p.getID() + 1);
			if (!o.isEmpty) {
				ProviderAssignment.this.channelCombo.setSelectedItem(o.toString());
			}
		}
	}

	public class ChannelSelected implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ProviderAssignment.this.messageLabel.setText("");
			ProviderAssignment.this.importButton.setText(ResourceManager.msg("IMPORT_TV"));
			int ix = ProviderAssignment.this.channelCombo.getSelectedIndex();

			if (ix >= 0) {
				ProviderAssignment.this.lastSelectedChannel = (String) ProviderAssignment.this.channelCombo
						.getItemAt(ix);
				Provider p = (Provider) ProviderAssignment.this.providerCombo.getSelectedItem();
				if (p == null)
					return;
				if (ix > 0) {
					int ics = ProviderAssignment.this.providerMaps.get(p.getID())
							.get(ProviderAssignment.this.lastSelectedChannel).getIndex();
					if (ics >= 0) {
						int tableLine = ProviderAssignment.this.sorter.convertRowIndexToView(ics);
						ProviderAssignment.this.table.setRowSelectionInterval(tableLine, tableLine);
						// table.scrollRectToVisible(table.getCellRect(tableLine,ix,true));
					}
				}
			}
		}
	}

	public class ChannelButtonsPressed implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ProviderAssignment.this.messageLabel.setText("");
			ProviderAssignment.this.importButton.setText(ResourceManager.msg("IMPORT_TV"));

			Provider p = (Provider) ProviderAssignment.this.providerCombo.getSelectedItem();
			if (p == null)
				return;

			TreeMap<String, ChannelAssignment> map = ProviderAssignment.this.providerMaps.get(p.getID());

			JButton button = (JButton) e.getSource();

			String channel = (String) ProviderAssignment.this.channelCombo.getEditor().getItem();

			if (button == ProviderAssignment.this.addChannelButton) {
				if (!map.containsKey(channel)) {
					map.put(channel, new ChannelAssignment(p.getID(), channel, -1));
					updateChannelComboBox();
					updateTableComboBoxes();
				}
			} else if (button == ProviderAssignment.this.modifyChannelButton) {
				if (ProviderAssignment.this.lastSelectedChannel != null
						&& ProviderAssignment.this.lastSelectedChannel.length() != 0) {
					if (!map.containsKey(channel)) {
						int ix = map.get(ProviderAssignment.this.lastSelectedChannel).getIndex();
						if (ix >= 0) {
							ChannelSet cs = ProviderAssignment.this.control.getChannelSets().get(ix);
							String id = cs.getChannel(p.getID()).getTextID();
							cs.remove(p.getID());
							cs.add(p.getID(), channel, id);
							map.remove(ProviderAssignment.this.lastSelectedChannel);
							map.put(channel, new ChannelAssignment(cs.getChannel(p.getID()), ix));
							updateChannelComboBox();
							updateTableComboBoxes();
							updateTable();
						}
					}
				}
			} else if (button == ProviderAssignment.this.importButton) {
				int count = p.importChannels();
				if (count >= 0) {
					p.updateChannelMap();
					updateIfChannelSetsChanged(p);
					ProviderAssignment.this.importButton.setText(ResourceManager.msg("SUCCESSFULL"));
					ProviderAssignment.this.messageLabel
							.setText(ResourceManager.msg("CHANNELS_IMPORTED", Integer.toString(count)));
				} else
					ProviderAssignment.this.importButton.setText(ResourceManager.msg("FAILED"));
			}
		}
	}

	public class TableChannelSelected implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			Provider p = (Provider) ProviderAssignment.this.providerCombo.getSelectedItem();
			if (p == null)
				return;
			int row = ProviderAssignment.this.table.getSelectedRow();
			if (row < 0)
				return;
			int selectedLine = ProviderAssignment.this.sorter.convertRowIndexToModel(row);
			MyTableObject o = (MyTableObject) ProviderAssignment.this.table.getModel().getValueAt(selectedLine,
					p.getID() + 1);
			if (!o.isEmpty) {
				ProviderAssignment.this.channelCombo.setSelectedItem(o.toString());
			}
		}
	}

	public class MyComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			if (o1.equalsIgnoreCase(o2))
				return o1.compareTo(o2);
			return o1.compareToIgnoreCase(o2);
		}
	}

	public ProviderAssignment(GUIPanel guiPanel) {
		super(guiPanel);

		this.columnNames = new String[Provider.getProviders().size() + 1];
		this.columnNames[0] = ResourceManager.msg("DVBVIEWER");

		int ip = 0;
		for (Provider provider : Provider.getProviders()) {
			int providerID = provider.getID();

			this.providerMaps.add(providerID, new TreeMap<String, ChannelAssignment>(new MyComparator()));

			this.updateMap(provider);

			// this. columnNames[ 0 ] = "" ;

			this.columnNames[ip++ + 1] = provider.getName();
		}
		this.table = new JTable(new MyTableModel());

	}

	public void updateMap(Provider provider) {
		int pid = provider.getID();

		TreeMap<String, ChannelAssignment> map = this.providerMaps.get(pid);
		map.clear();

		for (int ix = 0; ix < this.control.getChannelSets().size(); ix++) {
			ChannelSet cs = this.control.getChannelSets().get(ix);
			Channel c = cs.getChannel(pid);
			if (c == null)
				continue;
			map.put(c.getName(), new ChannelAssignment(c, ix));
		}
	}

	@Override
	public void init() {
		Provider defaultProvider = Provider.getProvider(this.control.getDefaultProvider());
		Insets i = new Insets(5, 5, 5, 5);
		GridBagConstraints c = null;

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		// c.gridwidth = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.unlockBox.addActionListener(new LockBoxChanged());
//		unlockBox.setText( GUIStrings.unlock() ) ;
		this.add(this.unlockBox, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		// c.gridwidth = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 0, 5, 5);

		JLabel lockLabel = new JLabel(ResourceManager.msg("UNLOCK"));
		this.add(lockLabel, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		// c.gridwidth = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		for (Provider provider : Provider.getProviders()) {
			this.providerCombo.addItem(provider);
		}
		this.providerCombo.addActionListener(new ProviderSelected());
		this.providerCombo.setSelectedItem(defaultProvider);
		this.add(this.providerCombo, c);

		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 1;
		// c.gridwidth = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.channelCombo.addActionListener(new ChannelSelected());
		this.channelCombo.setEditable(true);
		this.add(this.channelCombo, c);

		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 1;
		// c.gridwidth = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.addChannelButton.addActionListener(new ChannelButtonsPressed());
		this.addChannelButton.setText(ResourceManager.msg("ADD"));
		this.add(this.addChannelButton, c);

		c = new GridBagConstraints();
		c.gridx = 4;
		c.gridy = 1;
		// c.gridwidth = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.modifyChannelButton.addActionListener(new ChannelButtonsPressed());
		this.modifyChannelButton.setText(ResourceManager.msg("MODIFY"));
		this.add(this.modifyChannelButton, c);

		c = new GridBagConstraints();
		c.gridx = 5;
		c.gridy = 1;
		// c.gridwidth = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.importButton.addActionListener(new ChannelButtonsPressed());
		this.importButton.setText(ResourceManager.msg("IMPORT_TV"));
		this.add(this.importButton, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.insets = i;

		// this.deleteChannelButton.addActionListener( new ChannelButtonsPressed() ) ;
		JScrollPane scrollPane = new JScrollPane(this.table);
		// scrollPane.setPreferredSize( new Dimension(
		// 150+150*Provider.getProviders().size(),230 ) ) ;
		// scrollPane.setMinimumSize( new Dimension(
		// 150+150*Provider.getProviders().size(),230 ) ) ;
		scrollPane.setPreferredSize(new Dimension(150 + 150 * 3, 230));
		scrollPane.setMinimumSize(new Dimension(150 + 150 * 3, 230));
		this.table.setFillsViewportHeight(false);
		this.setupTable();
		this.add(scrollPane, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.messageLabel.setForeground(SystemColor.RED);
		this.messageLabel.setPreferredSize(new Dimension(200, 20));
		this.add(this.messageLabel, c);
	}

	private void updateChannelComboBox() {
		Provider provider = (Provider) this.providerCombo.getSelectedItem();
		int pid = provider.getID();
		this.channelCombo.removeAllItems();
		this.channelCombo.addItem("");
		for (String names : this.providerMaps.get(pid).keySet())
			this.channelCombo.addItem(names);
		this.channelCombo.setSelectedIndex(0);
	}

	private void setupTable() {
		this.sorter = new TableRowSorter<TableModel>(this.table.getModel());

		for (int i = 0; i <= Provider.getProviders().size(); i++)
			this.sorter.setComparator(i, new MyTableComparator());

		this.table.setRowSorter(this.sorter);

		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumn column = null;

		this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		column = this.table.getColumnModel().getColumn(0);
		column.setPreferredWidth(150);
//		column.setResizable( true ) ;
//		column.setPreferredWidth( 16 );
		for (Provider p : Provider.getProviders()) {
			int pid = p.getID();
			JComboBox<?> comboBox = new JComboBox<>();
			comboBox.addPopupMenuListener(new PopupCellChanged());
			column = this.table.getColumnModel().getColumn(pid + 1);
			column.setPreferredWidth(150);
			DefaultCellEditor cellEditor = new DefaultCellEditor(comboBox) {
				/**
				 *
				 */
				private static final long serialVersionUID = -5273432664600113545L;

				@Override
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
						int col) {
					String channel = value.toString();
					((JComboBox<?>) getComponent()).setSelectedItem(channel);
					return super.getTableCellEditorComponent(table, value, isSelected, row, col);
				}
			};
			column.setCellEditor(cellEditor);
		}
		updateTableComboBoxes();
		this.table.setRowSelectionAllowed(true);
		ListSelectionModel cellSelectionModel = this.table.getSelectionModel();
		cellSelectionModel.addListSelectionListener(new TableChannelSelected());
	}

	private void cancelCellEditing() {
		for (Provider p : Provider.getProviders()) {
			int pid = p.getID();
			TableColumn column = this.table.getColumnModel().getColumn(pid + 1);
			DefaultCellEditor editor = (DefaultCellEditor) column.getCellEditor();
			if (editor == null)
				return;
			editor.cancelCellEditing();
		}
	}

	private void updateTable() {
		((MyTableModel) this.table.getModel()).fireTableDataChanged();
	}

	private void updateTableComboBoxes() {
		for (Provider p : Provider.getProviders()) {
			int pid = p.getID();
			TableColumn column = this.table.getColumnModel().getColumn(pid + 1);
			DefaultCellEditor editor = (DefaultCellEditor) column.getCellEditor();
			@SuppressWarnings("unchecked")
			JComboBox<String> component = (JComboBox<String>) editor.getComponent();
			JComboBox<String> comboBox = component;
			comboBox.hidePopup();
			comboBox.removeAllItems();
			comboBox.addItem("");
			for (String name : this.providerMaps.get(pid).keySet()) {
				comboBox.addItem(name);
			}
		}
	}

	private void showTableLine(int line) {
		int tableLine = this.sorter.convertRowIndexToView(line);
		this.table.setRowSelectionInterval(tableLine, tableLine);
		this.table.scrollRectToVisible(this.table.getCellRect(tableLine, 0, true));
	}

	public class PopupCellChanged implements PopupMenuListener {

		@Override
		public void popupMenuCanceled(PopupMenuEvent arg0) {
			ProviderAssignment.this.messageLabel.setText("");
			ProviderAssignment.this.importButton.setText(ResourceManager.msg("IMPORT_TV"));
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
			cancelCellEditing();
			ProviderAssignment.this.messageLabel.setText("");
			ProviderAssignment.this.importButton.setText(ResourceManager.msg("IMPORT_TV"));
		}

		// @Override
		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			ProviderAssignment.this.messageLabel.setText("");
			ProviderAssignment.this.importButton.setText(ResourceManager.msg("IMPORT_TV"));
		}
	}

	private class MyTableObject extends Object {
		private final String channel;
		private final boolean isEmpty;
		private final ChannelSet channelSet;

		MyTableObject(String string, ChannelSet channelSet) {
			this.channel = string;
			this.isEmpty = (string.length() == 0);
			this.channelSet = channelSet;
		}

		@Override
		public String toString() {
			return this.channel;
		};

		public boolean isNullEntry() {
			return this.channelSet == null;
		};
	}

	public class MyTableComparator implements Comparator<MyTableObject> {
		@Override
		public int compare(MyTableObject o1, MyTableObject o2) {
			if (o1.isNullEntry() || o2.isNullEntry()) {
				if (o1.isNullEntry())
					return 1;
				else
					return -1;
			}
			if (o1.isEmpty || o2.isEmpty) {
				if (o1.isEmpty && o2.isEmpty)
					return 0;
				if (o1.isEmpty)
					return 1;
				return -1;
			} else if (o1.channel.equalsIgnoreCase(o2.channel))
				return o1.channel.compareTo(o2.channel);
			return o1.channel.compareToIgnoreCase(o2.channel);
		}
	}

	private class MyTableModel extends AbstractTableModel {
		/**
		 *
		 */
		private static final long serialVersionUID = -4564280852966603471L;

		/*
		 * private ImageIcon active = ResourceManager.createImageIcon(
		 * "icons/dvbViewer16.png", "DVBViewer icon" ) ; private ImageIcon inactive =
		 * ResourceManager.createImageIcon( "icons/dvbViewerEmpty16.png",
		 * "DVBViewer empty icon" ) ;
		 */ @Override
		public int getColumnCount() {
			return Provider.getProviders().size() + 1;
		}

		@Override
		public String getColumnName(int col) {
			return ProviderAssignment.this.columnNames[col];
		}

		@Override
		public Class<MyTableObject> getColumnClass(int col) {
			/*
			 * if ( col == 0 ) return ImageIcon.class ;
			 */ return MyTableObject.class;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return (col != 0);
		}

		@Override
		public int getRowCount() {
			return ProviderAssignment.this.control.getChannelSets().size() + 1;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (row < ProviderAssignment.this.control.getChannelSets().size()) {
				ChannelSet cs = ProviderAssignment.this.control.getChannelSets().get(row);
				if (col == 0) {
					String dvbViewer = cs.getDVBViewerChannel();
					if (dvbViewer != null) {
						String parts[] = dvbViewer.split("\\|");
						String out = parts[parts.length - 1];
						if (ProviderAssignment.this.control.getDVBViewer().getChannels().containsChannelID(dvbViewer))
							return new MyTableObject(out, cs);
					} else
						return new MyTableObject("", cs);
					/*
					 * return active ; else return inactive ;
					 */ }
				Channel channel = cs.getChannel(col - 1);
				if (channel != null)
					return new MyTableObject(channel.getName(), cs);
				else
					return new MyTableObject("", cs);
			} else {
				/*
				 * if ( col == 0 ) return "" ; return inactive ; else
				 */ return new MyTableObject("", null);
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			ProviderAssignment.this.messageLabel.setText("");
			ProviderAssignment.this.importButton.setText(ResourceManager.msg("IMPORT_TV"));
			cancelCellEditing();

			String channel = (String) value;

			int csid = row;
			int pid = col - 1;

			TreeMap<String, ChannelAssignment> channelMap = ProviderAssignment.this.providerMaps.get(pid);
			ChannelSet cs;
			if (csid < ProviderAssignment.this.control.getChannelSets().size())
				cs = ProviderAssignment.this.control.getChannelSets().get(csid);
			else {
				cs = new ChannelSet();
				ProviderAssignment.this.control.getChannelSets().add(cs);
			}

			if (channel.length() > 0) {
				ChannelAssignment pca = channelMap.get(channel);
				int pcsid = pca.getIndex();
				if (pcsid >= 0 && csid != pcsid) {
					showTableLine(pcsid);
					int answer = JOptionPane.showConfirmDialog(ProviderAssignment.this.guiPanel.getWindow(),
							"\"" + channel + "\" " + ResourceManager.msg("ASSIGNED_ERROR"), Constants.PROGRAM_NAME,
							JOptionPane.OK_CANCEL_OPTION);
					if (answer == JOptionPane.CANCEL_OPTION) {
						showTableLine(csid);
						return;
					}
					ChannelSet pcs = ProviderAssignment.this.control.getChannelSets().get(pcsid);
					pcs.remove(pid);
					pca.setIndex(-1);
					boolean isDeleted = removeChannelSetIfEmpty(pcsid);
					if (isDeleted && pcsid < csid)
						csid--;
				}
			}
			Channel pc = cs.getChannel(pid);
			if (pc != null)
				channelMap.put(pc.getName(), new ChannelAssignment(pc, -1));
			cs.remove(pid);

			if (channel.length() > 0) {
				String channelID = null;

				if (channelMap.containsKey(channel)) {
					channelID = channelMap.get(channel).getChannel().getTextID();
				}

				Channel c = cs.add(pid, channel, channelID);
				channelMap.put(channel, new ChannelAssignment(c, csid));
			}

			removeChannelSetIfEmpty(csid);
			updateTable();
			showTableLine(csid);

			ProviderAssignment.this.guiPanel.setChanged();
		}
	}

	private boolean removeChannelSetIfEmpty(int csid) {
		if (this.control.getChannelSets().get(csid).getChannels().size() != 0)
			return false;
		this.control.getChannelSets().remove(csid);
		for (TreeMap<String, ChannelAssignment> map : this.providerMaps) {
			for (Map.Entry<String, ChannelAssignment> entry : map.entrySet()) {
				int value = entry.getValue().listIndex;
				if (value == csid)
					entry.getValue().setIndex(-1);
				else if (value > csid)
					entry.getValue().setIndex(value - 1);
			}
		}
		return true;
	}

	public void updateIfChannelSetsChanged(Provider p) {
		if (p == null)
			for (Provider pl : Provider.getProviders())
				this.updateMap(pl);
		else
			this.updateMap(p);

		this.updateTable();
		this.updateTableComboBoxes();
		this.updateChannelComboBox();
	}

	@Override
	public void update(boolean active) {
		if (active) {
			this.unlockBox.setSelected(true);
			this.unlockBox.doClick();
		}
	}
}
