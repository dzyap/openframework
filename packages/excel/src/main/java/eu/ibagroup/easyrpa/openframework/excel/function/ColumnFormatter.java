package eu.ibagroup.easyrpa.openframework.excel.function;

import eu.ibagroup.easyrpa.openframework.excel.Cell;
import eu.ibagroup.easyrpa.openframework.excel.annotations.ExcelColumn;

/**
 * Allows to perform formatting for cells of specific table column on the sheet. Formatting means modification of
 * cell value and style based on some specific logic.
 *
 * @param <T> class of table records
 * @see ExcelColumn#formatter()
 */
@FunctionalInterface
public interface ColumnFormatter<T> {

    /**
     * Formats cells based on some specific logic.
     *
     * @param cell   the cell to which formatting is applied.
     * @param column name of column to which the cell belongs.
     * @param record the current record. Equals <b><code>null</code></b> if the cell is a <b>header cell</b>.
     */
    void format(Cell cell, String column, T record);
}
