/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2011 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.components.headertoolbar.actions;

import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.components.headertoolbar.actions.ResizeColumnCommand.ColumnGroupInfo;
import net.sf.jasperreports.components.headertoolbar.actions.ResizeColumnCommand.ColumnUtil;
import net.sf.jasperreports.components.table.BaseColumn;
import net.sf.jasperreports.components.table.StandardTable;
import net.sf.jasperreports.components.table.util.TableUtil;
import net.sf.jasperreports.web.commands.Command;
import net.sf.jasperreports.web.commands.CommandStack;

/**
 * @author Narcis Marcu (narcism@users.sourceforge.net)
 * @version $Id$
 */
public class MoveColumnCommand implements Command 
{
	
	private StandardTable table;
	private MoveColumnData moveColumnData;
//	private MoveColumnData oldMoveColumnData;
	private CommandStack individualResizeCommandStack;
	
	
	public MoveColumnCommand(StandardTable table, MoveColumnData moveColumnData) 
	{
		this.table = table;
		this.moveColumnData = moveColumnData;
		this.individualResizeCommandStack = new CommandStack();
	}

	
	public void execute() 
	{
		moveColumns(moveColumnData);
//		oldMoveColumnData = new MoveColumnData();
//		oldMoveColumnData.setColumnToMoveIndex(moveColumnData.getColumnToMoveNewIndex());
//		oldMoveColumnData.setColumnToMoveNewIndex(moveColumnData.getColumnToMoveIndex());
	}
	
	private void moveColumns(MoveColumnData moveColumnData) 
	{
		int srcColIndex = moveColumnData.getColumnToMoveIndex();
		int destColIndex = moveColumnData.getColumnToMoveNewIndex();
		
		List<BaseColumn> allColumns = TableUtil.getAllColumns(table);

		BaseColumn srcColumn = allColumns.get(srcColIndex);
		BaseColumn destColumn = allColumns.get(destColIndex);

		List<ColumnGroupInfo> srcColParentColumnGroups = new ColumnUtil(srcColIndex).getParentColumnGroups(table.getColumns());
		List<ColumnGroupInfo> destColParentColumnGroups = new ColumnUtil(destColIndex).getParentColumnGroups(table.getColumns());
	
		List<BaseColumn> srcSiblingColumns = 
			(srcColParentColumnGroups == null || srcColParentColumnGroups.isEmpty()) 
			? table.getColumns() 
			: srcColParentColumnGroups.get(srcColParentColumnGroups.size() - 1).columnGroup.getColumns();

		int srcSiblingColIndex = srcSiblingColumns.indexOf(srcColumn);
		int destSiblingColIndex = srcSiblingColumns.indexOf(destColumn);
		if (
			destSiblingColIndex < 0 
			&& (destColParentColumnGroups != null && !destColParentColumnGroups.isEmpty())
			)
		{
			Iterator<ColumnGroupInfo> it = destColParentColumnGroups.iterator();
			while (destSiblingColIndex < 0 && it.hasNext())
			{
				ColumnGroupInfo columnGroupInfo = it.next();
				destSiblingColIndex = srcSiblingColumns.indexOf(columnGroupInfo.columnGroup);
			}
		}
			
		if (destSiblingColIndex < 0)
		{
			//the dest column is not a sibling of the src column nor a descendant of a sibling; move is not possible
		}
		else if (srcSiblingColIndex != destSiblingColIndex)
		{
			individualResizeCommandStack.execute(new SimpleMoveColumnCommand(srcSiblingColumns, srcColumn, srcSiblingColIndex, destSiblingColIndex));
		}
	}

//	private void moveColumns(MoveColumnData moveColumnData) {
//		int colIndex = moveColumnData.getColumnToMoveIndex();
//		int newColIndex = moveColumnData.getColumnToMoveNewIndex();
//		
//		List<BaseColumn> allColumns = TableUtil.getAllColumns(table);
//		BaseColumn columnToMove = allColumns.get(colIndex);
//
//		if (colIndex != newColIndex) {
//			List<BaseColumn> groupColumns = getGroupColumnsForColumn(columnToMove, table.getColumns());
//			
//			if (groupColumns != null) {
//				groupColumns.remove(colIndex);
//				if (newColIndex == groupColumns.size()) {
//					groupColumns.add(columnToMove);
//				} else {
//					groupColumns.add(newColIndex, columnToMove);
//				}
//			}
//		}
//	}
//
//	private List<BaseColumn> getGroupColumnsForColumn(BaseColumn column, List<BaseColumn> columns) {
//		for (BaseColumn bc: columns) {
//			if (bc instanceof ColumnGroup) {
//				ColumnGroup cg = (ColumnGroup) bc;
//				if (cg.getColumns().contains(column)) {
//					return cg.getColumns();
//				} else {
//					return getGroupColumnsForColumn(column, cg.getColumns());
//				}
//			}
//			else {
//				return columns;
//			}
//		}
//		return null;
//	}
	
	public void undo() 
	{
		individualResizeCommandStack.undoAll();
	}

	public void redo() 
	{
		individualResizeCommandStack.redoAll();
	}

}