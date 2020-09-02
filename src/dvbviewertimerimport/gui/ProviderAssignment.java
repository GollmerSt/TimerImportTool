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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
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
import dvbviewertimerimport.misc.Log;
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

		@Override
		public String toString() {
			return this.channel.getUserName();
		}

		public Channel getChannel() {
			return this.channel;
		}

		public int getIndex() {
			return this.listIndex;
		}

		public void setIndex(int ix) {
			this.listIndex = ix;
		}
	}

	public class MyComboBox extends JComboBox<Channel> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8575973834032373480L;
		private Channel selectedChannel = null;
		private int currentProviderId = -1;

		public MyComboBox() {
			super();

			this.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Object o = e.getItem();
						if (o instanceof Channel) {
							MyComboBox.this.selectedChannel = (Channel) e.getItem();
						}
					} else {
//						MyComboBox.this.selectedChannel = null;
					}
				}

			});

			this.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ProviderAssignment.this.messageLabel.setText("");
					ProviderAssignment.this.importButton.setText(ResourceManager.msg("IMPORT_TV"));

					if (true) {
						Provider p = (Provider) ProviderAssignment.this.providerCombo.getSelectedItem();
						if (p == null || p.getID() != MyComboBox.this.currentProviderId) {
							return;
						}
						Channel channel = ProviderAssignment.this.channelCombo.getSelectedChannel();
						if (channel != null) {
							int ics = ProviderAssignment.this.providerMapsById.get(p.getID()).get(channel.getTextID())
									.getIndex();
							if (ics >= 0) {
								int tableLine = ProviderAssignment.this.sorter.convertRowIndexToView(ics);
								ProviderAssignment.this.table.setRowSelectionInterval(tableLine, tableLine);
								ProviderAssignment.this.table.scrollRectToVisible(
										ProviderAssignment.this.table.getCellRect(tableLine, p.getID() + 1, true));
							}
						}
					}
				}
			});

			this.setEditable(true);

		}

		public Channel getSelectedChannel() {
			return this.selectedChannel;
		}

		@Override
		public void removeAllItems() {
			this.selectedChannel = null;
			super.removeAllItems();
		}

		public void setCurrentProviderId(int currentProviderId) {
			this.currentProviderId = currentProviderId;
		}

	}

	private final ArrayList<Map<String, ChannelAssignment>> providerMapsById = //
			new ArrayList<>(); // Key: channelId
	private final ArrayList<Map<String, ChannelAssignment>> providerMapsByUserName = //
			new ArrayList<>(); // Key: channelId
	private final String[] columnNames;

	private final JCheckBox unlockBox = new JCheckBox();
	private final JComboBox<Provider> providerCombo = new JComboBox<>();
	private final MyComboBox channelCombo = new MyComboBox();
	private final JButton modifyChannelButton = new JButton();
	private final JButton importButton = new JButton();
	private final JTable table;
	private TableRowSorter<TableModel> sorter = null;

	private final JLabel messageLabel = new JLabel();

	private void enableByProvider(boolean enable) {
		Provider p = (Provider) this.providerCombo.getSelectedItem();

		this.modifyChannelButton.setEnabled(p.canModify() && enable);
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
			ProviderAssignment.this.setSelectedItemChannelCombo(o.getChannel());
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

			Map<String, ChannelAssignment> map = ProviderAssignment.this.providerMapsById.get(p.getID());

			JButton button = (JButton) e.getSource();

			Channel channel = (Channel) ProviderAssignment.this.channelCombo.getSelectedChannel();

			if (button == ProviderAssignment.this.modifyChannelButton) {
				Object item = ProviderAssignment.this.channelCombo.getSelectedItem();
				if (channel != null && item != null) {
					String userName = item.toString();
					if (!isUserNameDefined(userName, p.getID())) {
						int ix = map.get(channel.getIDKey()).getIndex();
						if (ix >= 0) {
							ChannelSet cs = ProviderAssignment.this.control.getChannelSets().get(ix);
							Channel c = cs.getChannel(p.getID());
							c.setUserName(userName);
							updateChannelComboBox();
							updateTableComboBoxes();
							updateTable();
						}
					}
				}
			} else if (button == ProviderAssignment.this.importButton) {
				int count = p.importChannels();
				if (count >= 0) {
					updateIfChannelSetsChanged(p);
					ProviderAssignment.this.importButton.setText(ResourceManager.msg("SUCCESSFULL"));
					ProviderAssignment.this.messageLabel
							.setText(ResourceManager.msg("CHANNELS_IMPORTED", Integer.toString(count)));
				} else
					ProviderAssignment.this.importButton.setText(ResourceManager.msg("FAILED"));
			}
		}
	}

	private boolean isUserNameDefined(String userName, int pid) {
		Map<String, ChannelAssignment> map = ProviderAssignment.this.providerMapsById.get(pid);
		for (ChannelAssignment assignment : map.values()) {
			if (assignment.channel.getUserName().equals(userName)) {
				return true;
			}
		}
		return false;
	}

	public class TableChannelSelected implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			int row = ProviderAssignment.this.table.getSelectedRow();
			if (row < 0)
				return;
			int column = ProviderAssignment.this.table.getSelectedColumn();
			if (column > 0 && ProviderAssignment.this.table.getSelectedColumnCount() == 1) {
				int selectedLine = ProviderAssignment.this.sorter.convertRowIndexToModel(row);
				MyTableObject o = (MyTableObject) ProviderAssignment.this.table.getModel().getValueAt(selectedLine,
						column);
				ProviderAssignment.this.providerCombo.setSelectedIndex(o.getPid());
				ProviderAssignment.this.setSelectedItemChannelCombo(o.getChannel());
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

			this.providerMapsById.add(providerID, new HashMap<String, ChannelAssignment>());
			this.providerMapsByUserName.add(providerID, new TreeMap<>(new MyComparator()));

			this.updateMap(provider);

			// this. columnNames[ 0 ] = "" ;

			this.columnNames[ip++ + 1] = provider.getName();
		}
		this.table = new JTable(new MyTableModel());

	}

	public void updateMap(Provider provider) {
		int pid = provider.getID();

		Map<String, ChannelAssignment> mapById = this.providerMapsById.get(pid);
		mapById.clear();

		Map<String, ChannelAssignment> mapByUserName = this.providerMapsByUserName.get(pid);
		mapByUserName.clear();

		for (int ix = 0; ix < this.control.getChannelSets().size(); ix++) {
			ChannelSet cs = this.control.getChannelSets().get(ix);
			Channel c = cs.getChannel(pid);
			if (c == null)
				continue;
			ChannelAssignment assignment = new ChannelAssignment(c, ix);
			mapById.put(c.getTextID(), assignment);
			mapByUserName.put(c.getUserName(), assignment);
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

		this.add(this.channelCombo, c);

		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 1;
		// c.gridwidth = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.modifyChannelButton.addActionListener(new ChannelButtonsPressed());
		this.modifyChannelButton.setText(ResourceManager.msg("MODIFY"));
		this.add(this.modifyChannelButton, c);

		c = new GridBagConstraints();
		c.gridx = 4;
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
		this.channelCombo.setCurrentProviderId(pid);
		this.channelCombo.addItem(null);
		for (ChannelAssignment assignment : this.providerMapsByUserName.get(pid).values()) {
			Channel c = assignment.channel;
			this.channelCombo.addItem(new Channel(c) {
				@Override
				public String toString() {
					return c.getUserName();
				}
			});
		}
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
			JComboBox<String> comboBox = new JComboBox<>();
			comboBox.addPopupMenuListener(new PopupCellChanged());
			column = this.table.getColumnModel().getColumn(pid + 1);
			column.setPreferredWidth(150);
			column.setCellEditor(new MyCellEditor(comboBox));
		}
		updateTableComboBoxes();
		this.table.setRowSelectionAllowed(true);
		ListSelectionModel cellSelectionModel = this.table.getSelectionModel();
		cellSelectionModel.addListSelectionListener(new TableChannelSelected());
	}

	private class MyCellEditor extends DefaultCellEditor {
		/**
		 *
		 */
		private static final long serialVersionUID = -5273432664600113545L;

		public MyCellEditor(JComboBox<String> comboBox) {
			super(comboBox);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
			MyTableObject tableObject = (MyTableObject) value;
			String channel = tableObject.toString();
			((JComboBox<?>) getComponent()).setSelectedItem(channel);

			ProviderAssignment.this.providerCombo.setSelectedIndex(tableObject.getPid());
			ProviderAssignment.this.setSelectedItemChannelCombo(tableObject.getChannel());

			return super.getTableCellEditorComponent(table, value, isSelected, row, col);
		}
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
			for (String name : this.providerMapsByUserName.get(pid).keySet()) {
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

	private class MyTableObject {
		private final Integer pid;
		private final boolean isEmpty;
		private final ChannelSet channelSet;

		MyTableObject(Integer pid, ChannelSet channelSet) {
			this.pid = pid;
			this.channelSet = channelSet;
			this.isEmpty = (this.toString() == null);
		}

		@Override
		public String toString() {
			if (this.channelSet != null) {
				if (this.pid == null) {
					return this.channelSet.getDVBViewerChannel();
				} else {
					Channel c = this.channelSet.getChannel(this.pid);
					if (c != null) {
						return c.getUserName();
					}
				}
			}
			return null;
		}

		public boolean isNullEntry() {
			return this.channelSet == null;
		}

		public Channel getChannel() {
			if (this.channelSet != null && this.pid != null) {
				return this.channelSet.getChannel(this.pid);
			} else {
				return null;
			}
		}

		public Integer getPid() {
			return this.pid;
		}
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
			} else if (o1.toString().equalsIgnoreCase(o2.toString())) {
				return o1.toString().compareTo(o2.toString());
			}
			return o1.toString().compareToIgnoreCase(o2.toString());
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
					return new MyTableObject(null, cs);
				}
				return new MyTableObject(col - 1, cs);
			} else {
				return new MyTableObject(col, null);
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			ProviderAssignment.this.messageLabel.setText("");
			ProviderAssignment.this.importButton.setText(ResourceManager.msg("IMPORT_TV"));
			cancelCellEditing();

			MyTableObject tableObject = (MyTableObject) this.getValueAt(row, col);

			Channel tableCellChannel = tableObject.getChannel();

			int currentId = row;
			int pid = col - 1;
			String userChannel = (String) value;

			boolean assign = userChannel.length() > 0;

			ChannelSet channelSet = tableObject.channelSet;

			Map<String, ChannelAssignment> channelMap = ProviderAssignment.this.providerMapsByUserName.get(pid);

			// Shift the selected channel to the end of the table

			if (assign) {
				// shift the selected channel to the selected position
				ChannelAssignment assignment = channelMap.get(userChannel);
				if (channelSet != null) {
					int formerId = assignment.getIndex();
					if (formerId >= 0 && currentId != formerId) {
						showTableLine(formerId);
						int answer = JOptionPane.showConfirmDialog(ProviderAssignment.this.guiPanel.getWindow(),
								"\"" + userChannel + "\" " + ResourceManager.msg("ASSIGNED_ERROR"),
								Constants.PROGRAM_NAME, JOptionPane.OK_CANCEL_OPTION);
						if (answer == JOptionPane.CANCEL_OPTION) {
							showTableLine(currentId);
							return;
						}

						MyTableObject formerObject = (MyTableObject) this.getValueAt(formerId, col);
						ChannelSet formerChannelSet = formerObject.channelSet;
						formerChannelSet.remove(pid);

						assignment.setIndex(currentId);
						channelSet.add(assignment.getChannel());

						if (formerChannelSet.isEmpty()) {
							boolean isDeleted = removeChannelSetIfEmpty(formerId);
							if (isDeleted && formerId < currentId)
								--currentId;
						}

					} else {
						return;
					}
				}
			}
			
			// move the former channel to the end of the list

			if (tableCellChannel != null) {

				ChannelAssignment tableCellAssignment = channelMap.get(tableCellChannel.getUserName());

				ChannelSet newSet = new ChannelSet();
				newSet.add(tableCellChannel);

				if (!assign) {
					channelSet.remove(pid);
				}

				ProviderAssignment.this.control.getChannelSets().add(newSet);

				if (!assign && channelSet.isEmpty()) {

					removeChannelSetIfEmpty(tableCellAssignment.getIndex());
				}

				channelMap.get(tableCellChannel.getUserName())
						.setIndex(ProviderAssignment.this.control.getChannelSets().size() - 1);

			}
			updateTable();
			showTableLine(currentId);

			ProviderAssignment.this.guiPanel.setChanged();
		}

	}

	private boolean removeChannelSetIfEmpty(int csid) {
		if (this.control.getChannelSets().get(csid).getChannels().size() != 0) {
			return false;
		}
		this.control.getChannelSets().remove(csid);
		for (int pid = 0; pid < Provider.getProviders().size(); ++pid) {
			for (ChannelAssignment assignment : this.providerMapsByUserName.get(pid).values()) {
				int id = assignment.getIndex();
				if (id > csid) {
					assignment.setIndex(id - 1);
				} else if (id == csid) {
					Log.error("Assignment of channel \"" + assignment.getChannel().getUserName() + "\" isn't moved.");
				}
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

	private void setSelectedItemChannelCombo(Channel channel) {
		if (channel != null) {
			int ix = ((DefaultComboBoxModel<Channel>) ProviderAssignment.this.channelCombo.getModel())
					.getIndexOf(channel);
			ProviderAssignment.this.channelCombo.setSelectedIndex(ix);
		}
	}
}
