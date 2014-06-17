package com.josephliccini.groovegator;

import javax.swing.table.DefaultTableModel;

public class GrooveGatorTableModel extends DefaultTableModel
{
	private static final long serialVersionUID = 5893720845719418900L;

	public GrooveGatorTableModel(Object[][] tableValues,
			Object[] tableHeaders)
	{
		super(tableValues, tableHeaders);
	}

	@Override
	public Class<?> getColumnClass(int column)
	{
		if (column == 0)
			return Boolean.class;
		else
			return String.class;
	}	

}
