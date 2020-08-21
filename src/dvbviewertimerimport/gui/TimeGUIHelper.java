package dvbviewertimerimport.gui;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import dvbviewertimerimport.misc.ResourceManager;

public class TimeGUIHelper {
	
	private static final SimpleDateFormat dayTimeFormat ;
	private static final String dateString = ResourceManager.msg( "DATE_STRING" ) ;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat( dateString ) ;
	
	
	
	static
	{
		dayTimeFormat =  new SimpleDateFormat("HH:mm");
//		dayTimeFormat.setTimeZone( TimeZone.getTimeZone("GMT0") ) ;
	}

	public static class DayTimeRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4237289392266042360L;
		public DayTimeRenderer()
		{ 
			super();
			this.setHorizontalAlignment(JLabel.LEFT) ;
		}
		public void setValue(Object value) {
			long time = ((Long)value).longValue() ;
			String d = longToDayTime( time );
			this.setText(d);
		}
	}
	public static class DayTimeEditor extends AbstractCellEditor implements TableCellEditor
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2756852706034748338L;
		private final JSpinner spinner ;
		
		public DayTimeEditor()
		{
			SpinnerDateModel model = new SpinnerDateModel(new Date(0), null, null, Calendar.MINUTE ) ;
			this.spinner = new JSpinner( model ) ;
			this.spinner.setEditor(new JSpinner.DateEditor(this.spinner, "HH:mm"));   	}
		@Override
		public Component getTableCellEditorComponent(
					JTable table,
					Object object,
					boolean isSelected,
					int row,
					int column)
		{
			long dayTime = ((Long)object).longValue() ;
			GregorianCalendar calendar = new GregorianCalendar( TimeZone.getTimeZone("GMT0") ) ;
			calendar.setTimeInMillis( dayTime ) ;
			Date value = calendar.getTime()  ;
			this.spinner.setValue( value ) ;
			return this.spinner ;
		}
		@Override
		public Object getCellEditorValue() {
			long result = ((Date)this.spinner.getValue()).getTime() ;
			return new Long( result ) ;
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

	public static class DateRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4237289392266042360L;
		public DateRenderer()
		{ 
			super();
			this.setHorizontalAlignment(JLabel.LEFT) ;
		}
		public void setValue(Object value) {
			long time = ((Long)value).longValue() ;
			String d = longToDate( time );
			this.setText(d);
		}
	}
	public static class DateEditor extends AbstractCellEditor implements TableCellEditor
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2756852706034748338L;
		private final JSpinner spinner ;
		
		public DateEditor()
		{
			SpinnerDateModel model = new SpinnerDateModel(new Date(0), null, null, Calendar.DATE ) ;
			this.spinner = new JSpinner( model ) ;
			this.spinner.setEditor(new JSpinner.DateEditor(this.spinner, dateString ));   	}
		@Override
		public Component getTableCellEditorComponent(
					JTable table,
					Object object,
					boolean isSelected,
					int row,
					int column)
		{
			long dayTime = ((Long)object).longValue() ;
			Date value = new Date( dayTime )  ;
			this.spinner.setValue( value ) ;
			return this.spinner ;
		}
		@Override
		public Object getCellEditorValue() {
			long result = ((Date)this.spinner.getValue()).getTime() ;
			return new Long( result ) ;
		}
		@Override
		public boolean shouldSelectCell(EventObject arg0) {
			return true;
		}
	}
	public static String longToDate( long d )
	{
		return dateFormat.format( new Date( d ) ) ;
	}
}
