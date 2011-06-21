// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import dvbviewertimerimport.control.OffsetEntry;
import dvbviewertimerimport.control.TimeOffsets;
import dvbviewertimerimport.misc.ResourceManager;

public class OffsetsDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5037000535757796243L;

	private static final SimpleDateFormat dayTimeFormat ;

	private final TimeOffsets offsets ;
	private final TimeOffsets originalOffsets ;
	private final boolean isGlobal ;
	
	private final JTable table = new JTable() ;
	private final String[] columnNames = new String[ 7 ];
	private final JButton okButton = new JButton() ;
	private final JButton cancelButton = new JButton() ;
	private final JButton newButton = new JButton() ; 
	private final JButton deleteButton = new JButton() ; 
	private final JCheckBox globalCheckBox = new JCheckBox() ; 

	private static ImageIcon programIcon   = ResourceManager.createImageIcon( "icons/dvbViewer Programm16.png", "DVBViewer icon" ) ;

	private final GUIPanel guiPanel ;
	
	static
	{
		dayTimeFormat =  new SimpleDateFormat("HH:mm");
		//dayTimeFormat.setTimeZone( TimeZone.getTimeZone("GMT0") ) ;
	}

	private class ButtonsPressed implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton)e.getSource() ;
			if ( source == okButton )
			{
				guiPanel.setChanged() ;
				table.editingStopped( null ) ;
				offsets.setUseGlobal( ! globalCheckBox.isSelected() ) ;
				originalOffsets.assign( offsets ) ;
				dispose() ;
			}
			else if ( source == cancelButton )
				dispose() ;
			else if ( source == newButton )
			{
				table.editingStopped( null ) ;
				offsets.addEmpty() ;
				((MyTableModel)table.getModel()).fireTableDataChanged() ;
			}
			else if ( source == deleteButton )
			{
				table.editingStopped( null ) ;
				int line = table.getSelectedRow() ;
				if ( line < 0 )
					return ;
				offsets.delete( line ) ;
				((MyTableModel)table.getModel()).fireTableDataChanged() ;
			}
		}
		
	}
	
	public OffsetsDialog( final GUIPanel guiPanel, final TimeOffsets offsets, final boolean isGlobal )
	{
		super( guiPanel.getWindow() , java.awt.Dialog.ModalityType.APPLICATION_MODAL  ) ;

		this.isGlobal = isGlobal ;
		this.originalOffsets = offsets ;
		this.offsets = offsets.clone() ;
		this.guiPanel = guiPanel ;
		
		
		this.columnNames[0] = ResourceManager.msg( "TIME_BEFORE" ) ;
		this.columnNames[1] = ResourceManager.msg( "TIME_AFTER" ) ;
		this.columnNames[2] = ResourceManager.msg( "WEEKDAYS" ) ;
		this.columnNames[3] = ResourceManager.msg( "START" ) ;
		this.columnNames[4] = ResourceManager.msg( "END" ) ;
		
		this.setIconImage( OffsetsDialog.programIcon.getImage() ) ;
		
		init() ;
	}
	public void init()
	{
		this.setTitle( ResourceManager.msg( "OFFSET_DIALOG" ) ) ;
		this.setLayout(  new GridBagLayout() ) ;
		
		Insets i = new Insets( 5, 5, 5, 5 );
		GridBagConstraints c = null ;


		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
		
		this.globalCheckBox.setText( ResourceManager.msg( "IGNORE_GLOBAL_OFFSET_SETTING" ) );
		this.add( this.globalCheckBox, c ) ;
		this.globalCheckBox.setVisible( ! this.isGlobal ) ;
		this.globalCheckBox.setSelected( ! this.offsets.getUseGlobal() ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 1 ;
		c.weightx    = 1.0 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		JScrollPane scrollPane = new JScrollPane( this.table );
		scrollPane.setPreferredSize( new Dimension( 420,200 ) ) ;
		scrollPane.setMinimumSize( new Dimension( 420,200 ) ) ;
		this.table.setFillsViewportHeight(false);
		this.setupTable() ;
		this.add( scrollPane, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 0 ;
		c.gridy      = 2 ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
		
		this.newButton.addActionListener( new ButtonsPressed() ) ;
		this.newButton.setText( ResourceManager.msg( "NEW_ENTRY" ) ) ;
		this.add( this.newButton, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 1 ;
		c.gridy      = 2 ;
		c.anchor     = GridBagConstraints.NORTHWEST ;
		c.insets     = i ;
		
		this.deleteButton.addActionListener( new ButtonsPressed() ) ;
		this.deleteButton.setText( ResourceManager.msg( "DELETE" ) ) ;
		this.add( this.deleteButton, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 2 ;
		c.gridy      = 2 ;
		c.weightx    = 0.5 ;
		c.anchor     = GridBagConstraints.NORTHEAST ;
		//c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.okButton.addActionListener( new ButtonsPressed() ) ;
		this.okButton.setText( ResourceManager.msg( "OK" ) ) ;
		this.add( this.okButton, c ) ;

		
		c = new GridBagConstraints();
		c.gridx      = 3 ;
		c.gridy      = 2 ;
		c.gridwidth  = GridBagConstraints.REMAINDER ;
		//c.fill       = GridBagConstraints.HORIZONTAL ;
		c.insets     = i ;
		
		this.cancelButton.addActionListener( new ButtonsPressed() ) ;
		this.cancelButton.setText( ResourceManager.msg( "CANCEL" ) ) ;
		this.add( this.cancelButton, c ) ;

		
		
		this.pack(); 
		this.setLocationRelativeTo(null);
		this.setVisible( true );
		
	}
	private void setupTable()
	{
		this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		this.table.setModel( new MyTableModel() ) ;
				
		table.setRowHeight( 22 ) ;
		TableColumn column = null ;
		column = this.table.getColumnModel().getColumn( 0 ) ;
		column.setCellEditor( new SpinnerNumberEditor( 0, 999, 1 ) ) ;
		column.setPreferredWidth( 60 ) ;
		
		column = this.table.getColumnModel().getColumn( 1 ) ;
		column.setCellEditor( new SpinnerNumberEditor( 0, 999, 1 ) ) ;
		column.setPreferredWidth( 60 ) ;
		
		column = this.table.getColumnModel().getColumn( 2 ) ;
		column.setCellRenderer( new WeekDayRenderer() ) ;
		column.setCellEditor( new WeekDayEditor() ) ;
		column.setPreferredWidth( 22*7+2 ) ;
		
		column = this.table.getColumnModel().getColumn( 3 ) ;
		column.setCellRenderer( new TimeGUIHelper.DayTimeRenderer() ) ;
		column.setCellEditor( new TimeGUIHelper.DayTimeEditor() ) ;
		column.setPreferredWidth( 65 ) ;
		
		column = this.table.getColumnModel().getColumn( 4 ) ;
		column.setCellRenderer( new TimeGUIHelper.DayTimeRenderer() ) ;
		column.setCellEditor( new TimeGUIHelper.DayTimeEditor() ) ;
		column.setPreferredWidth( 65 ) ;
		
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ) ;
	}
	class WeekDayObject
	{
		public final boolean [] weekdays ;
		WeekDayObject()
		{
			this.weekdays = new boolean[7] ;
		}
		WeekDayObject( boolean[] weekdays )
		{
			this.weekdays = weekdays ;
		}
		public String toString()
		{
			String result = "" ;
			for ( int ix = 0 ; ix < this.weekdays.length ; ix++ )
			{
				if ( this.weekdays[ ix ] )
					result += "+" ;
				else
					result += "-" ;
			}
			return result ;
		}
		public boolean[] getWeekDays() { return this.weekdays ; } ;
	}
	private class MyTableModel extends AbstractTableModel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1024744241487034753L;
		

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 5 ;
		}
		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}
		@Override
		public Class<?> getColumnClass(int col)
		{
			if ( col == 2 )
				return WeekDayObject.class ;
			return Integer.class ;
		}
		@Override
		public boolean isCellEditable(int row, int col)
		{
			return true ;
		}
		@Override
		public int getRowCount() {
			return offsets.size() ; // + 1 ;
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			OffsetEntry entry = offsets.getOffset( row ) ;
			int minutes[] = entry.getMinutes() ;
			
			switch ( col )
			{
			case 0 :
			case 1 :
				int offset = minutes[ col ] ;
				if ( offset < 0 )
					offset = 0 ;
				return new Integer( offset ) ;
			case 2 :
				return new WeekDayObject( entry.getWeekDays() ) ;
			case 3 :
			case 4 :
				long dayTime = 0L;
				try {
					dayTime = dayTimeToLong( entry.getDayTimes()[ col - 3 ] );
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return new Long( dayTime ) ;
			}
			return null ;
		}
		@Override
		public void setValueAt(Object value, int row, int col)
		{
			OffsetEntry entry = offsets.getOffset( row ) ;
			int minutes[] = entry.getMinutes() ;
			
			switch ( col )
			{
			case 0 :
			case 1 :
				int offset =((Integer) value).intValue() ;
				if ( offset == 0 )
					offset = -1 ;
				minutes[ col ] = offset ;
				break ;
			case 2 :
				for ( int ix = 0 ; ix < 7 ; ix ++ )
					entry.getWeekDays()[ix] = ((WeekDayObject)value).getWeekDays()[ix] ;
				break ;
			case 3 :
			case 4 :
				long dayTime = ((Long)value).longValue() ;
				//if ( dayTime == Constants.LASTMINUTE )
				//	dayTime = Constants.DAYMILLSEC ;
				entry.getDayTimes()[ col - 3 ] = longToDayTime( dayTime ) ;
				break ;
			}
		}
	}
	public class WeekDayRenderer extends JPanel implements TableCellRenderer
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3374633243517567009L;
		private final JCheckBox[] checkBoxes = new JCheckBox[ 7 ] ;
		private Color originalBackGround ;
		private Color selectedBackGround = new JTextField().getSelectionColor() ;
		
		public WeekDayRenderer() {
			((FlowLayout)this.getLayout()).setHgap( 0 ) ;
			((FlowLayout)this.getLayout()).setVgap( 0 ) ;
			for ( int ix = 0 ; ix < this.checkBoxes.length ; ix++ )
			{
				JCheckBox checkBox = new JCheckBox() ;
				this.checkBoxes[ ix ] = checkBox ;
				this.add( checkBox ) ;
				checkBox.setHorizontalAlignment(SwingConstants.CENTER);
			}
			this.originalBackGround = this.checkBoxes[ 0 ].getBackground() ;
		}
		@Override
		public Component getTableCellRendererComponent(
					JTable table,
					Object object,
					boolean isSelected,
					boolean hasFocus,
					int row,
					int column)
		{
			boolean[] weekDays = ((WeekDayObject)object).getWeekDays() ;
			for ( int ix = 0 ; ix < weekDays.length ; ix++ )
			{
				JCheckBox checkBox = this.checkBoxes[ ix ] ;
				checkBox.setSelected( weekDays[ ix ] ) ;
				if ( isSelected )
					checkBox.setBackground( selectedBackGround ) ;
				else
					checkBox.setBackground( this.originalBackGround ) ;
	 		}
			return this ;
		}
	}
	public class WeekDayEditor extends AbstractCellEditor implements TableCellEditor
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5188930711938065159L;
		private final JPanel panel = new JPanel() ;
		private final JCheckBox[] checkBoxes = new JCheckBox[ 7 ] ;
		private Color selectedBackGround = new JTextField().getSelectionColor() ;
		
		public WeekDayEditor() {
			((FlowLayout)panel.getLayout()).setHgap( 0 ) ;
			((FlowLayout)panel.getLayout()).setVgap( 0 ) ;
			for ( int ix = 0 ; ix < this.checkBoxes.length ; ix++ )
			{
				JCheckBox checkBox = new JCheckBox() ;
				checkBox.setBackground( selectedBackGround ) ;
				this.checkBoxes[ ix ] = checkBox ;
				panel.add( checkBox ) ;
			}
			this.checkBoxes[ 0 ].getBackground();
		}
		@Override
		public Component getTableCellEditorComponent(
					JTable table,
					Object object,
					boolean isSelected,
					int row,
					int column)
		{
			boolean[] weekDays = ((WeekDayObject)object).getWeekDays() ;
			for ( int ix = 0 ; ix < weekDays.length ; ix++ )
			{
				JCheckBox checkBox = this.checkBoxes[ ix ] ;
				checkBox.setSelected( weekDays[ ix ] ) ;
			}
			return panel ;
		}
		@Override
		public Object getCellEditorValue() {
			WeekDayObject result = new WeekDayObject() ;
			boolean[] days = result.getWeekDays() ; 
			for ( int ix = 0 ; ix < days.length ; ix++ )
			{
				days[ ix ] = this.checkBoxes[ ix ].isSelected() ;
			}
			return result;
		}
		@Override
		public boolean shouldSelectCell(EventObject arg0) {
			return true;
		}
	}
	public class SpinnerNumberEditor extends AbstractCellEditor implements TableCellEditor
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8810972718831377646L;
		private final JSpinner spinner ;
		
		public SpinnerNumberEditor( int start, int end, int step )
		{
			spinner = new JSpinner(new SpinnerNumberModel( start, start, end, step ) ) ;
		}
		@Override
		public Component getTableCellEditorComponent(
					JTable table,
					Object object,
					boolean isSelected,
					int row,
					int column)
		{
			int value = ((Integer)object).intValue() ;
			spinner.setValue( value ) ;
			return spinner ;
		}
		@Override
		public Object getCellEditorValue() {
			return spinner.getValue() ;
		}
		@Override
		public boolean shouldSelectCell(EventObject arg0) {
			return true;
		}
	}
	public static String longToDayTime( long d )
	{
		return dayTimeFormat.format( new Date( d ) ) ;
	}
	public static long dayTimeToLong( String t ) throws ParseException
	{
		return dayTimeFormat.parse( t ).getTime() ;
	}

}
