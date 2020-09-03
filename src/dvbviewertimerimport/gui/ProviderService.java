// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import dvbviewertimerimport.dvbviewer.DVBViewerService;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.provider.OutDatedInfo;
import dvbviewertimerimport.provider.Provider;

public class ProviderService extends MyTabPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 460262656692083356L;

	private final JComboBox<Provider> providerCombo = new JComboBox<>();
	private final JCheckBox lockBox = new JCheckBox();
	private final JTextField urlBox = new JTextField();
	private final JLabel userNameLabel = new JLabel();
	private final JTextField userNameBox = new JTextField();
	private final JLabel passwordLabel = new JLabel();
	private final JTextField passwordBox = new JTextField();
	private final JSpinner triggerBox = new JSpinner();

	private final JCheckBox enableSinceBox = new JCheckBox();
	private final JLabel sinceLabel = new JLabel();
	private final JSpinner sinceBox = new JSpinner();
	private final JLabel sinceSyncLabel = new JLabel();
	private final JSpinner sinceSyncBox = new JSpinner();

	private final JCheckBox verboseBox = new JCheckBox();
	private final JCheckBox messageBox = new JCheckBox();
	private final JCheckBox mergeBox = new JCheckBox();
	private final JCheckBox filterBox = new JCheckBox();
	private final JButton providerButton = new JButton();
	private final JButton installButton = new JButton();
	private final JButton uninstallButton = new JButton();

	private final JCheckBox serviceEnableBox = new JCheckBox();
	private final JLabel urlServiceLabel = new JLabel();
	private final JTextField urlServiceBox = new JTextField();
	private final JLabel userServiceLabel = new JLabel();
	private final JTextField userServiceBox = new JTextField();
	private final JLabel passwordServiceLabel = new JLabel();
	private final JTextField passwordServiceBox = new JTextField();
	private final JCheckBox wolEnableBox = new JCheckBox();

	private final JLabel waitTimeLabel = new JLabel();
	private final JSpinner waitTimeBox = new JSpinner();
	private final JLabel broadCastLabel = new JLabel();
	private final JTextField broadCastBox = new JTextField();
	private final JLabel macLabel = new JLabel();
	private final JTextField macBox = new JTextField();
	private final JButton serviceButton = new JButton();

	private ProviderCheckBoxesChanged providerCheckBoxesListener = null;
	private boolean providerCheckBoxesListenerEnabled = false;

	private Provider lastProvider = null;

	private void setOutDated(final OutDatedInfo o) {
		OutDatedInfo i = o;
		boolean inhibit = false;
		boolean enable = true;
		if (i == null) {
			i = new OutDatedInfo();
			inhibit = true;
			enable = false;
		}
		enable = i.isEnabled() ? enable : false;

		this.enableSinceBox.setEnabled(!inhibit);
		this.sinceLabel.setEnabled(!inhibit);
		this.sinceBox.setEnabled(enable);
		this.sinceSyncLabel.setEnabled(!inhibit);
		this.sinceSyncBox.setEnabled(enable);

		this.enableSinceBox.setSelected(i.isEnabled());
		this.sinceBox.setValue(i.getMissingSince());
		this.sinceSyncBox.setValue(i.getMissingSyncSince());
	}

	public class ProviderSelected implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ProviderService.this.lockBox.setSelected(false);
			JComboBox<?> cb = (JComboBox<?>) e.getSource();
			Object o = cb.getSelectedItem();
			if (o == null)
				return;
			setListenerProviderCheckBoxes(false);
			updateProvider();
			Provider p = (Provider) cb.getSelectedItem();
			ProviderService.this.lastProvider = p;
			boolean hasURL = p.hasURL();
			boolean hasAccount = p.hasAccount();
			ProviderService.this.lockBox.setEnabled(hasURL);
			if (ProviderService.this.lockBox.isSelected())
				ProviderService.this.urlBox.setEnabled(hasURL);
			else
				ProviderService.this.urlBox.setEnabled(false);
			ProviderService.this.userNameLabel.setEnabled(hasAccount);
			ProviderService.this.userNameBox.setEnabled(hasAccount);
			ProviderService.this.passwordLabel.setEnabled(hasAccount);
			ProviderService.this.passwordBox.setEnabled(hasAccount);
			ProviderService.this.triggerBox.setValue(p.getTriggerAction());
			ProviderService.this.providerButton.setEnabled(p.canTest());
			ProviderService.this.providerButton.setText(ResourceManager.msg("CHECK"));
			if (hasURL)
				ProviderService.this.urlBox.setText(p.getURL());
			else
				ProviderService.this.urlBox.setText("");
			if (hasAccount) {
				ProviderService.this.userNameBox.setText(p.getUserName());
				ProviderService.this.passwordBox.setText(p.getPassword());
			} else {
				ProviderService.this.userNameBox.setText("");
				ProviderService.this.passwordBox.setText("");

			}
			setOutDated(p.getOutDatedLimits());
			ProviderService.this.verboseBox.setSelected(p.getVerbose());
			ProviderService.this.messageBox.setSelected(p.getMessage());
			ProviderService.this.mergeBox.setSelected(p.getMerge());
			ProviderService.this.filterBox.setSelected(p.isFiltered());
			ProviderService.this.installButton.setText(ResourceManager.msg("INSTALL"));
			ProviderService.this.uninstallButton.setText(ResourceManager.msg("UNINSTALL"));
			ProviderService.this.installButton.setEnabled(p.mustInstall());
			ProviderService.this.uninstallButton.setEnabled(p.mustInstall());
			setListenerProviderCheckBoxes(true);

		}
	}

	public class LockBoxChanged implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			Provider p = (Provider) ProviderService.this.providerCombo.getSelectedItem();
			if (p.hasURL())
				ProviderService.this.urlBox.setEnabled(ProviderService.this.lockBox.isSelected());
		}
	}

	public void setListenerProviderCheckBoxes(boolean enable) {
		if (this.providerCheckBoxesListener == null)
			this.providerCheckBoxesListener = new ProviderCheckBoxesChanged();

		if (this.providerCheckBoxesListenerEnabled == enable)
			return;

		this.providerCheckBoxesListenerEnabled = enable;

		if (enable) {
			this.verboseBox.addItemListener(this.providerCheckBoxesListener);
			this.messageBox.addItemListener(this.providerCheckBoxesListener);
			this.mergeBox.addItemListener(this.providerCheckBoxesListener);
			this.filterBox.addItemListener(this.providerCheckBoxesListener);
		} else {
			this.verboseBox.removeItemListener(this.providerCheckBoxesListener);
			this.messageBox.removeItemListener(this.providerCheckBoxesListener);
			this.mergeBox.removeItemListener(this.providerCheckBoxesListener);
			this.filterBox.removeItemListener(this.providerCheckBoxesListener);
		}
	}

	public class ProviderCheckBoxesChanged implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			Provider p = (Provider) ProviderService.this.providerCombo.getSelectedItem();
			JCheckBox source = (JCheckBox) e.getItemSelectable();
			if (source == ProviderService.this.verboseBox)
				p.setVerbose(source.isSelected());
			else if (source == ProviderService.this.messageBox)
				p.setMessage(source.isSelected());
			else if (source == ProviderService.this.mergeBox)
				p.setMerge(source.isSelected());
			else if (source == ProviderService.this.filterBox)
				p.setFilter(source.isSelected());
			else if (source == ProviderService.this.enableSinceBox && p.getOutDatedLimits() != null) {
				p.getOutDatedLimits().setEnabled(source.isSelected());
				setOutDated(p.getOutDatedLimits());
			}
			ProviderService.this.guiPanel.setChanged();
		}
	}

	public class ProviderTestButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			updateProvider();
			Provider p = (Provider) ProviderService.this.providerCombo.getSelectedItem();
			if (p.canTest()) {
				String buttonText;
				String testResult = p.test();
				if (p.test() != null) {
					buttonText = ResourceManager.msg(testResult);
				} else {
					buttonText = ResourceManager.msg("FAILED");
				}
				ProviderService.this.providerButton.setText(buttonText);
			}
		}
	}

	public class ProviderInstallButtons implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			updateProvider();
			Provider p = (Provider) ProviderService.this.providerCombo.getSelectedItem();
			if (p.mustInstall()) {
				JButton button = (JButton) e.getSource();
				if (button == ProviderService.this.installButton) {
					if (p.install())
						ProviderService.this.installButton.setText(ResourceManager.msg("SUCCESSFULL"));
					else
						ProviderService.this.installButton.setText(ResourceManager.msg("FAILED"));
					ProviderService.this.uninstallButton.setText(ResourceManager.msg("UNINSTALL"));
				} else if (button == ProviderService.this.uninstallButton) {
					if (p.uninstall())
						ProviderService.this.uninstallButton.setText(ResourceManager.msg("SUCCESSFULL"));
					else
						ProviderService.this.uninstallButton.setText(ResourceManager.msg("FAILED"));
					ProviderService.this.installButton.setText(ResourceManager.msg("INSTALL"));
				}
			}
		}
	}

	public class ServiceCheckBoxesChanged implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			DVBViewerService dvbs = ProviderService.this.control.getDVBViewer().getService();
			Object source = e.getItemSelectable();
			boolean serviceEnabled = ProviderService.this.serviceEnableBox.isSelected();
			if (source == ProviderService.this.serviceEnableBox) {
				ProviderService.this.urlServiceLabel.setEnabled(serviceEnabled);
				ProviderService.this.urlServiceBox.setEnabled(serviceEnabled);
				ProviderService.this.userServiceLabel.setEnabled(serviceEnabled);
				ProviderService.this.userServiceBox.setEnabled(serviceEnabled);
				ProviderService.this.passwordServiceLabel.setEnabled(serviceEnabled);
				ProviderService.this.passwordServiceBox.setEnabled(serviceEnabled);
				ProviderService.this.serviceButton.setEnabled(serviceEnabled);
				ProviderService.this.wolEnableBox.setEnabled(serviceEnabled);
				if (serviceEnabled != dvbs.isEnabled()) {
					dvbs.setEnabled(serviceEnabled);
					ProviderService.this.guiPanel.setChanged();
					ProviderService.this.guiPanel.updateIfServiceChanged();
				}
			}
			if (source == ProviderService.this.wolEnableBox || source == ProviderService.this.serviceEnableBox) {
				boolean enabled = ProviderService.this.wolEnableBox.isSelected();
				if (enabled != dvbs.getEnableWOL()) {
					dvbs.setEnableWOL(enabled);
					ProviderService.this.guiPanel.setChanged();
				}
				enabled = enabled && serviceEnabled;
				ProviderService.this.waitTimeLabel.setEnabled(enabled);
				ProviderService.this.waitTimeBox.setEnabled(enabled);
				ProviderService.this.broadCastLabel.setEnabled(enabled);
				ProviderService.this.broadCastBox.setEnabled(enabled);
				ProviderService.this.macLabel.setEnabled(enabled);
				ProviderService.this.macBox.setEnabled(enabled);
			}
		}
	}

	public class ServiceTestButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			updateService();
			long version;
			try {
				version = ProviderService.this.control.getDVBViewer().getService().readVersion(true);
			} catch (ErrorClass e1) {
				version = -1;
			}
			String buttonText = null;
			if (version < 0)
				buttonText = ResourceManager.msg("FAILED");
			else {
				long high = version / 10000000;
				version %= 10000000;
				long mid = version / 100000;
				version %= 100000;
				long low = version / 1000;
				version %= 1000;
				buttonText = "Version: " + Long.toString(high) + "." + Long.toString(mid) + "." + Long.toString(low)
						+ "." + Long.toString(version);
			}
			((JButton) e.getSource()).setText(buttonText);
		}

	}

	public ProviderService(GUIPanel guiPanel) {
		super(guiPanel);
	}

	@Override
	public void init() {
		Provider defaultProvider = Provider.getProvider(this.control.getDefaultProvider());

		TitledBorder tB = null;
		GridBagConstraints c = null;

		JPanel providerBox = new JPanel(new GridBagLayout());

		tB = BorderFactory.createTitledBorder(ResourceManager.msg("PROVIDER"));
		providerBox.setBorder(tB);

		Insets i = new Insets(5, 5, 5, 5);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.providerCombo.addActionListener(new ProviderSelected());
		for (Provider p : Provider.getProviders())
			this.providerCombo.addItem(p);
		this.providerCombo.setSelectedItem(defaultProvider);
		this.lastProvider = defaultProvider;
		providerBox.add(this.providerCombo, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.lockBox.setText(ResourceManager.msg("URL"));
		this.lockBox.setHorizontalAlignment(SwingConstants.RIGHT);
		this.lockBox.addItemListener(new LockBoxChanged());
		providerBox.add(this.lockBox, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		providerBox.add(this.urlBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.userNameLabel.setText(ResourceManager.msg("USER_NAME"));
		providerBox.add(this.userNameLabel, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		providerBox.add(this.userNameBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.passwordLabel.setText(ResourceManager.msg("PASSWORD"));
		providerBox.add(this.passwordLabel, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		providerBox.add(this.passwordBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		JLabel triggerLabel = new JLabel(ResourceManager.msg("TRIGGER_ACTION"));
		providerBox.add(triggerLabel, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
//		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = i;

		SpinnerModel model = new SpinnerNumberModel(defaultProvider.getTriggerAction(), // initial value
				-1, 1 << 15, 1);
		this.triggerBox.setModel(model);
		providerBox.add(this.triggerBox, c);

		JPanel checkBoxPanel = new JPanel(new GridBagLayout());

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.enableSinceBox.addItemListener(new ProviderCheckBoxesChanged());
		checkBoxPanel.add(this.enableSinceBox, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.sinceLabel.setText(ResourceManager.msg("MISSING_SINCE"));
		checkBoxPanel.add(this.sinceLabel, c);

		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
//		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = i;

		model = new SpinnerNumberModel(0, // initial value
				0, 99, 1);
		this.sinceBox.setModel(model);
		checkBoxPanel.add(this.sinceBox, c);

		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 0;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.sinceSyncLabel.setText(ResourceManager.msg("MISSING_SINCE_SYNC"));
		checkBoxPanel.add(this.sinceSyncLabel, c);

		c = new GridBagConstraints();
		c.gridx = 4;
		c.gridy = 0;
		// c.gridwidth = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = i;

		model = new SpinnerNumberModel(0, // initial value
				0, 99, 1);
		this.sinceSyncBox.setModel(model);
		checkBoxPanel.add(this.sinceSyncBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		// c.weightx = 0.5 ;
		c.gridwidth = 2;
		c.insets = i;

		this.verboseBox.setText(ResourceManager.msg("VERBOSE"));
		checkBoxPanel.add(this.verboseBox, c);

		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 1;
		c.weightx = 0.5;
		// c.fill = GridBagConstraints.HORIZONTAL ;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.messageBox.setText(ResourceManager.msg("MESSAGE"));
		checkBoxPanel.add(this.messageBox, c);

		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 1;
		c.weightx = 0.5;
		// c.fill = GridBagConstraints.HORIZONTAL ;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.mergeBox.setText(ResourceManager.msg("MERGE"));
		checkBoxPanel.add(this.mergeBox, c);

		c = new GridBagConstraints();
		c.gridx = 4;
		c.gridy = 1;
		c.weightx = 0.5;
		// c.fill = GridBagConstraints.HORIZONTAL ;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.filterBox.setText(ResourceManager.msg("FILTER"));
		checkBoxPanel.add(this.filterBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		c.weightx = 0.5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;

		providerBox.add(checkBoxPanel, c);

		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 6;
		c.insets = i;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;

		this.providerButton.setText(ResourceManager.msg("CHECK"));
		this.providerButton.addActionListener(new ProviderTestButton());
		providerBox.add(this.providerButton, c);

		JPanel installPanel = new JPanel(new GridBagLayout());

		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 0, 0);
		;

		this.installButton.setText(ResourceManager.msg("INSTALL"));
		this.installButton.addActionListener(new ProviderInstallButtons());
		installPanel.add(this.installButton, c);

		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHEAST;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		c.insets = new Insets(0, 0, 0, 0);
		;

		this.uninstallButton.setText(ResourceManager.msg("UNINSTALL"));
		this.uninstallButton.addActionListener(new ProviderInstallButtons());
		installPanel.add(this.uninstallButton, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 7;
		c.insets = i;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;

		providerBox.add(installPanel, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;

		// providerBox.setPreferredSize( new Dimension( 380, 350 ) ) ;
		this.add(providerBox, c);
		this.setOutDated(defaultProvider.getOutDatedLimits());

		DVBViewerService dvbs = this.control.getDVBViewer().getService();

		JPanel serviceBox = new JPanel(new GridBagLayout());

		tB = BorderFactory.createTitledBorder(ResourceManager.msg("DVBVIEWER_SERVICE"));
		serviceBox.setBorder(tB);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = i;

		this.serviceEnableBox.setText(ResourceManager.msg("ENABLE"));
		this.serviceEnableBox.setSelected(!dvbs.isEnabled());
		this.serviceEnableBox.addItemListener(new ServiceCheckBoxesChanged());
		this.serviceEnableBox.setSelected(dvbs.isEnabled());
		serviceBox.add(this.serviceEnableBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.urlServiceLabel.setText(ResourceManager.msg("URL"));
		serviceBox.add(this.urlServiceLabel, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.urlServiceBox.setText(dvbs.getURL());
		serviceBox.add(this.urlServiceBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.userServiceLabel.setText(ResourceManager.msg("USER_NAME"));
		serviceBox.add(this.userServiceLabel, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.userServiceBox.setText(dvbs.getUserName());
		serviceBox.add(this.userServiceBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.passwordServiceLabel.setText(ResourceManager.msg("PASSWORD"));
		serviceBox.add(this.passwordServiceLabel, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.passwordServiceBox.setText(dvbs.getPassword());
		serviceBox.add(this.passwordServiceBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = i;

		this.wolEnableBox.setText(ResourceManager.msg("WOL"));
		this.wolEnableBox.setSelected(!dvbs.getEnableWOL());
		this.wolEnableBox.addItemListener(new ServiceCheckBoxesChanged());
		this.wolEnableBox.setSelected(dvbs.getEnableWOL());
		serviceBox.add(this.wolEnableBox, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.waitTimeLabel.setText(ResourceManager.msg("WAIT_TIME"));
		serviceBox.add(this.waitTimeLabel, c);

		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 4;
//		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = i;

		SpinnerModel waitModel = new SpinnerNumberModel(dvbs.getWaitTimeAfterWOL(), // initial value
				-1, 60, 1);
		this.waitTimeBox.setModel(waitModel);
		serviceBox.add(this.waitTimeBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.broadCastLabel.setText(ResourceManager.msg("BROAD_CAST_ADDRESS"));
		serviceBox.add(this.broadCastLabel, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 5;
		c.weightx = 0.5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.broadCastBox.setText(dvbs.getBroadCastAddress());
		serviceBox.add(this.broadCastBox, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 6;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = i;

		this.macLabel.setText(ResourceManager.msg("MAC_ADDRESS"));
		serviceBox.add(this.macLabel, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 6;
		c.weightx = 0.5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = i;

		this.macBox.setText(dvbs.getMacAddress());
		serviceBox.add(this.macBox, c);

		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 7;
		c.insets = i;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;

		this.serviceButton.setText(ResourceManager.msg("CHECK"));
		this.serviceButton.addActionListener(new ServiceTestButton());
		serviceBox.add(this.serviceButton, c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		// c.weighty = 0.5 ;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;

		serviceBox.setPreferredSize(new Dimension(300, 300));
		this.add(serviceBox, c);

		this.setListenerProviderCheckBoxes(true);
	}

	public void updateLockBox() {
		this.lockBox.setSelected(false);
	}

	public void updateProvider() {
		if (this.lastProvider != null) {
			if (!this.lastProvider.getURL().equals(this.urlBox.getText())) {
				this.lastProvider.setURL(this.urlBox.getText());
				this.guiPanel.setChanged();
			}
			if (this.lastProvider.getUserName() != null
					&& !this.lastProvider.getUserName().equals(this.userNameBox.getText())) {
				this.lastProvider.setUserName(this.userNameBox.getText());
				this.guiPanel.setChanged();
			}
			if (this.lastProvider.getPassword() != null
					&& !this.lastProvider.getPassword().equals(this.passwordBox.getText())) {
				this.lastProvider.setPassword(this.passwordBox.getText());
				this.guiPanel.setChanged();
			}
			int trigger = ((Integer) this.triggerBox.getValue()).intValue();
			if (this.lastProvider.getTriggerAction() != trigger) {
				this.lastProvider.setTriggerAction(trigger);
				this.guiPanel.setChanged();
			}
			if (this.lastProvider.getOutDatedLimits() != null) {
				OutDatedInfo i = this.lastProvider.getOutDatedLimits();
				int missingSince = ((Integer) this.sinceBox.getValue()).intValue();
				if (missingSince != i.getMissingSince()) {
					i.setMissingSince(missingSince);
					this.guiPanel.setChanged();
				}
				int missingSyncSince = ((Integer) this.sinceSyncBox.getValue()).intValue();
				if (missingSyncSince != i.getMissingSyncSince()) {
					i.setMissingSyncSince(missingSyncSince);
					this.guiPanel.setChanged();
				}
			}
		}
	}

	public void updateService() {
		DVBViewerService dvbs = this.control.getDVBViewer().getService();
		if (!dvbs.getURL().equals(this.urlServiceBox.getText())) {
			dvbs.setURL(this.urlServiceBox.getText());
			this.guiPanel.setChanged();
		}
		if (!dvbs.getUserName().equals(this.userServiceBox.getText())) {
			dvbs.setUserName(this.userServiceBox.getText());
			this.guiPanel.setChanged();
		}
		if (!dvbs.getPassword().equals(this.passwordServiceBox.getText())) {
			dvbs.setPassword(this.passwordServiceBox.getText());
			this.guiPanel.setChanged();
		}
		int waitTime = (Integer) this.waitTimeBox.getValue();
		if (dvbs.getWaitTimeAfterWOL() != waitTime) {
			dvbs.setWaitTimeAfterWOL(waitTime);
			this.guiPanel.setChanged();
		}
		if (!dvbs.getBroadCastAddress().equals(this.broadCastBox.getText())) {
			dvbs.setBroadCastAddress(this.broadCastBox.getText());
			this.guiPanel.setChanged();
		}
		if (!dvbs.getMacAddress().equals(this.macBox.getText())) {
			dvbs.setMacAddress(this.macBox.getText());
			this.guiPanel.setChanged();
		}
	}

	@Override
	public void update(boolean active) {
		if (active) {
			this.updateLockBox();
			/*
			 * } else {
			 */ this.updateProvider();
			this.updateService();
		}
	}

}
