package eu.ibagroup.easyrpa.openframework.excel;

import eu.ibagroup.easyrpa.openframework.excel.constants.InsertMethod;
import eu.ibagroup.easyrpa.openframework.excel.constants.MatchMethod;
import eu.ibagroup.easyrpa.openframework.excel.constants.SortDirection;
import eu.ibagroup.easyrpa.openframework.excel.internal.PoiElementsCache;
import eu.ibagroup.easyrpa.openframework.excel.utils.TypeUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Sheet implements Iterable<Row> {

    private ExcelDocument parent;

    private int documentId;

    private int sheetIndex;

    protected Sheet(ExcelDocument parent, int sheetIndex) {
        this.parent = parent;
        this.documentId = parent.getId();
        this.sheetIndex = sheetIndex;
    }

    public ExcelDocument getDocument() {
        return parent;
    }

    public int getIndex() {
        return sheetIndex;
    }

    public String getName() {
        return getPoiSheet().getSheetName();
    }

    public Cell getCell(String cellRef) {
        CellRef ref = new CellRef(cellRef);
        return getCell(ref.getRow(), ref.getCol());
    }

    public Cell getCell(int rowIndex, int colIndex) {
        Row row = getRow(rowIndex);
        return row != null ? row.getCell(colIndex) : null;
    }

    public Object getValue(String cellRef) {
        CellRef ref = new CellRef(cellRef);
        return getValue(ref.getRow(), ref.getCol(), Object.class);
    }

    public <T> T getValue(String cellRef, Class<T> valueType) {
        CellRef ref = new CellRef(cellRef);
        return getValue(ref.getRow(), ref.getCol(), valueType);
    }

    public Object getValue(int rowIndex, int colIndex) {
        return getValue(rowIndex, colIndex, Object.class);
    }

    public <T> T getValue(int rowIndex, int colIndex, Class<T> valueType) {
        Cell cell = getCell(rowIndex, colIndex);
        return cell != null ? cell.getValue(valueType) : null;
    }

    public void setValue(String cellRef, Object value) {
        CellRef ref = new CellRef(cellRef);
        setValue(ref.getRow(), ref.getCol(), value);
    }

    public void setValue(int rowIndex, int colIndex, Object value) {
        if (rowIndex >= 0 && colIndex >= 0) {
            Row row = getRow(rowIndex);
            if (row == null) {
                row = createRow(rowIndex);
            }
            row.setValue(colIndex, value);
        }
    }

    public List<List<Object>> getValues() {
        return getRange(getFirstRowIndex(), getFirstColumnIndex(), getLastRowIndex(), getLastColumnIndex());
    }

    public void setValues(List<List<?>> values) {
        putRange(0, 0, values);
    }

    public List<List<Object>> getRange(String startRef, String endRef) {
        CellRef sRef = new CellRef(startRef);
        CellRef eRef = new CellRef(endRef);
        return getRange(sRef.getRow(), sRef.getCol(), eRef.getRow(), eRef.getCol());
    }

    public List<List<Object>> getRange(int startRow, int startCol, int endRow, int endCol) {
        List<List<Object>> data = new ArrayList<>();

        if (startRow < 0 || startCol < 0 || endRow < 0 || endCol < 0) {
            return data;
        }

        int r1 = Math.min(startRow, endRow);
        int r2 = Math.max(startRow, endRow);
        int c1 = Math.min(startCol, endCol);
        int c2 = Math.max(startCol, endCol);

        for (int row = r1; row <= r2; row++) {
            List<Object> rowList = new ArrayList<>();
            for (int col = c1; col <= c2; col++) {
                rowList.add(getValue(row, col));
            }
            data.add(rowList);
        }
        return data;
    }

    public void putRange(String startRef, List<?> data) {
        CellRef sRef = new CellRef(startRef);
        putRange(sRef.getRow(), sRef.getCol(), data);
    }

    public void putRange(int startRow, int startCol, List<?> data) {
        if (data != null && data.size() > 0) {
            if (!(data.get(0) instanceof List)) {
                data = Collections.singletonList(data);
            }
            int rowIndex = startRow;
            for (Object rowList : data) {
                if (rowList instanceof List) {
                    Row row = getRow(rowIndex);
                    if (row == null) {
                        row = createRow(rowIndex);
                    }
                    row.putRange(startCol, (List<?>) rowList);
                    rowIndex++;
                }
            }
        }
    }

    public Row getRow(String rowRef) {
        return getRow(new CellRef(rowRef).getRow());
    }

    public Row getRow(int rowIndex) {
        if (rowIndex >= 0) {
            org.apache.poi.ss.usermodel.Row row = getPoiSheet().getRow(rowIndex);
            return row != null ? new Row(this, rowIndex) : null;
        }
        return null;
    }

    public Row findRow(String... values) {
        return findRow(MatchMethod.EXACT, values);
    }

    public Row findRow(MatchMethod matchMethod, String... values) {
        if (matchMethod == null) {
            matchMethod = MatchMethod.EXACT;
        }
        for (Row row : this) {
            if (row == null) {
                continue;
            }
            boolean matchesFound = false;
            for (String key : values) {
                matchesFound = false;
                for (Cell cell : row) {
                    if (cell == null) {
                        continue;
                    }
                    matchesFound = matchMethod.match(cell.getValue(String.class), key);
                    if (matchesFound) {
                        break;
                    }
                }
                if (!matchesFound) {
                    break;
                }
            }

            if (matchesFound) {
                return row;
            }
        }
        return null;
    }

    public Row createRow(int rowIndex) {
        getPoiSheet().createRow(rowIndex);
        return new Row(this, rowIndex);
    }

    public void insertRows(InsertMethod method, String startCellRef, List<?> data) {
        CellRef ref = new CellRef(startCellRef);
        insertRows(method, ref.getRow(), ref.getCol(), data);
    }

    public void insertRows(InsertMethod method, int rowPos, int startCol, List<?> data) {
        if (rowPos < 0 || startCol < 0 || data == null || data.isEmpty()) {
            return;
        }

        int rowIndex = method == null || method == InsertMethod.BEFORE ? rowPos : rowPos + 1;

        if (rowIndex > getLastRowIndex()) {
            putRange(rowIndex, startCol, data);

        } else {
            if (!(data.get(0) instanceof List)) {
                data = Collections.singletonList(data);
            }
            shiftRows(rowIndex, data.size());
            putRange(rowIndex, startCol, data);
        }
    }

    public void removeRow(String rowRef) {
        removeRow(new CellRef(rowRef).getRow());
    }

    public void removeRow(Row row) {
        removeRow(row.getIndex());
    }

    public void removeRow(int rowIndex) {
        int lastRowIndex = getLastRowIndex();
        if (rowIndex < 0 || rowIndex > lastRowIndex) {
            return;
        }
        if (rowIndex == lastRowIndex) {
            org.apache.poi.ss.usermodel.Sheet poiSheet = getPoiSheet();
            org.apache.poi.ss.usermodel.Row row = poiSheet.getRow(rowIndex);
            if (row != null) {
                poiSheet.removeRow(row);
            }
        } else {
            shiftRows(rowIndex + 1, -1);
        }
    }

    public void cleanRow(String rowRef) {
        cleanRow(new CellRef(rowRef).getRow());
    }

    public void cleanRow(Row row) {
        cleanRow(row.getIndex());
    }

    public void cleanRow(int rowIndex) {
        org.apache.poi.ss.usermodel.Row row = getPoiSheet().getRow(rowIndex);
        if (row != null) {
            while (row.getLastCellNum() >= 0) {
                row.removeCell(row.getCell(row.getLastCellNum()));
            }
        }
    }

    public int getFirstRowIndex() {
        return getPoiSheet().getFirstRowNum();
    }

    public int getLastRowIndex() {
        return getPoiSheet().getLastRowNum();
    }

    public Column getColumn(String colRef) {
        return getColumn(new CellRef(colRef).getCol());
    }

    public Column getColumn(int colIndex) {
        return colIndex >= 0 ? new Column(this, colIndex) : null;
    }

    public void addColumn(List<?> values) {
        List<List<?>> columnData = values.stream().map(Collections::singletonList).collect(Collectors.toList());
        putRange(0, getLastColumnIndex() + 1, columnData);
    }

    public void insertColumn(InsertMethod method, String startCellRef, List<?> data) {
        CellRef ref = new CellRef(startCellRef);
        insertColumn(method, ref.getCol(), ref.getRow(), data);
    }

    public void insertColumn(InsertMethod method, int columnPos, int startRow, List<?> values) {
        if (columnPos < 0 || startRow < 0 || values == null || values.isEmpty()) {
            return;
        }

        int columnIndex = method == null || method == InsertMethod.BEFORE ? columnPos : columnPos + 1;
        List<List<?>> columnData = values.stream().map(Collections::singletonList).collect(Collectors.toList());

        if (columnIndex <= getLastColumnIndex()) {
            shiftColumns(columnIndex, 1);
        }
        putRange(startRow, columnIndex, columnData);
    }


    public void moveColumn(String columnToMoveRef, InsertMethod method, String toPositionRef) {
        moveColumn(new CellRef(columnToMoveRef).getCol(), method, new CellRef(toPositionRef).getCol());
    }

    public void moveColumn(int columnToMoveIndex, InsertMethod method, int toPositionIndex) {
        //TODO Implement this
        // via VBS
    }

    public void removeColumn(String colRef) {
        removeColumn(new CellRef(colRef).getCol());
    }

    public void removeColumn(Column column) {
        removeColumn(column.getIndex());
    }

    public void removeColumn(int colIndex) {
        int lastColumnIndex = getLastColumnIndex();
        if (colIndex < 0 || colIndex > lastColumnIndex) {
            return;
        }
        if (colIndex == lastColumnIndex) {
            cleanColumn(colIndex);
        } else {
            shiftColumns(colIndex, -1);
        }
    }

    public void cleanColumn(String colRef) {
        cleanColumn(new CellRef(colRef).getCol());
    }

    public void cleanColumn(Column column) {
        cleanColumn(column.getIndex());
    }

    public void cleanColumn(int colIndex) {
        for (org.apache.poi.ss.usermodel.Row row : getPoiSheet()) {
            if (row != null) {
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(colIndex);
                if (cell != null) {
                    row.removeCell(cell);
                }
            }
        }
    }

    public void setColumnWidth(String colRef, int width) {
        setColumnWidth(new CellRef(colRef).getCol(), width);
    }

    public void setColumnWidth(int columnIndex, int width) {
        if (width > 255) {
            throw new IllegalArgumentException("Column width cannot be more than 255.");
        }
        getPoiSheet().setColumnWidth(columnIndex, width * 256);
    }

    public void filterColumn(String columnRef, List<Object> valuesToFilter, MatchMethod exact) {
        //TODO Implement this
        // via VBS
    }

    public void sortColumn(String columnRef, SortDirection direction) {
        //TODO Implement this
        // via VBS
    }

    public int getFirstColumnIndex() {
        org.apache.poi.ss.usermodel.Sheet poiSheet = getPoiSheet();
        int firstColIndex = -1;
        int firstRowNum = poiSheet.getFirstRowNum();
        if (firstRowNum >= 0) {
            firstColIndex = Integer.MAX_VALUE;
            for (org.apache.poi.ss.usermodel.Row row : poiSheet) {
                firstColIndex = Math.min(firstColIndex, row.getFirstCellNum());
            }
        }
        return firstColIndex;
    }

    public int getLastColumnIndex() {
        org.apache.poi.ss.usermodel.Sheet poiSheet = getPoiSheet();
        int lastColIndex = -1;
        int firstRowNum = poiSheet.getFirstRowNum();
        if (firstRowNum >= 0) {
            for (org.apache.poi.ss.usermodel.Row row : poiSheet) {
                lastColIndex = Math.max(lastColIndex, row.getLastCellNum());
            }
        }
        return lastColIndex;
    }

    public <T> Table<T> getTable(String topLeftCellRef, Class<T> recordType) {
        CellRef ref = new CellRef(topLeftCellRef);
        return getTable(ref.getRow(), ref.getCol(), recordType);
    }

    public <T> Table<T> getTable(int headerTopRow, int headerLeftCol, Class<T> recordType) {
        return new Table<T>(this, headerTopRow, headerLeftCol, headerTopRow, getLastColumnIndex(), recordType);
    }

    public <T> Table<T> getTable(String headerTopLeftCellRef, String headerBottomRightCellRef, Class<T> recordType) {
        CellRef tlRef = new CellRef(headerTopLeftCellRef);
        CellRef brRef = new CellRef(headerBottomRightCellRef);
        return getTable(tlRef.getRow(), tlRef.getCol(), brRef.getRow(), brRef.getCol(), recordType);
    }

    public <T> Table<T> getTable(int headerTopRow, int headerLeftCol,
                                 int headerBottomRow, int headerRightCol, Class<T> recordType) {
        return new Table<T>(this, headerTopRow, headerLeftCol, headerBottomRow, headerRightCol, recordType);
    }

    /**
     * lookup table on the sheet with specified keywords in header.
     *
     * @param recordType - class instance of related records
     * @param keywords   - keywords to localize table header
     * @return instance of Table or <code>null</code> if nothing was found.
     */
    public <T> Table<T> findTable(Class<T> recordType, String... keywords) {
        return findTable(recordType, MatchMethod.EXACT, keywords);
    }

    /**
     * lookup table on the sheet with specified keywords in header.
     *
     * @param recordType  - class instance of related records
     * @param matchMethod - method of matching keywords with table column names
     * @param keywords    - keywords to localize table header
     * @return instance of Table or <code>null</code> if nothing was found.
     */
    public <T> Table<T> findTable(Class<T> recordType, MatchMethod matchMethod, String... keywords) {
        if (matchMethod == null) {
            matchMethod = MatchMethod.EXACT;
        }
        Row headerRow = findRow(matchMethod, keywords);
        if (headerRow != null) {
            CellRef headerRef = headerRow.getReference();
            return getTable(headerRef.getRow(), headerRef.getCol(), recordType);
        }
        return null;
    }

    public <T> Table<T> insertTable(List<T> records) {
        return insertTable(0, 0, records);
    }

    public <T> Table<T> insertTable(String topLeftCellRef, List<T> records) {
        CellRef ref = new CellRef(topLeftCellRef);
        return insertTable(ref.getRow(), ref.getCol(), records);
    }

    public <T> Table<T> insertTable(int startRow, int startCol, List<T> records) {
        return startRow >= 0 && startCol >= 0 && records != null && records.size() > 0
                ? new Table<T>(this, startRow, startCol, records)
                : null;
    }

    /**
     * Exports sheet to PDF
     *
     * @param pdfFilePath
     */
    public void exportToPDF(String pdfFilePath) {
        //TODO Implement this
//        runScript(new ExportToPDF(getActiveSheet().getSheetName(), pdfFilePath));
    }

    public void addImage(String pathToImage, String fromCellRef, String toCellRef) {
        //TODO Implement this
    }


    @Override
    public Iterator<Row> iterator() {
        return new RowIterator(getPoiSheet());
    }

    public org.apache.poi.ss.usermodel.Sheet getPoiSheet() {
        return PoiElementsCache.getPoiSheet(documentId, sheetIndex);
    }

    private void shiftColumns(int startCol, int columnsCount) {
        //TODO Implement this
        // via VBS
    }

    private void shiftRows(int startRow, int rowsCount) {
        org.apache.poi.ss.usermodel.Sheet poiSheet = getPoiSheet();
        int endRow = poiSheet.getLastRowNum();

        if (startRow < 0 || startRow > endRow) {
            return;
        }

        poiSheet.shiftRows(startRow, endRow, rowsCount);

        //Rows have been shifted and their positions changed. We need to cleanup
        // caches to get actual poi elements.
        PoiElementsCache.clearRowsAndCellsCache(documentId);

        // Shift data validation ranges separately since by default shifting of rows
        // doesn't affect position of data validation
        List<? extends DataValidation> dataValidations = poiSheet.getDataValidations();

        try {
            //Cleanup all data validations
            if (poiSheet instanceof XSSFSheet) {
                ((XSSFSheet) poiSheet).getCTWorksheet().unsetDataValidations();
            } else if (poiSheet instanceof SXSSFSheet) {
                XSSFSheet xssfSheet = TypeUtils.getFieldValue(poiSheet, "_sh");
                xssfSheet.getCTWorksheet().unsetDataValidations();
            } else if (poiSheet instanceof HSSFSheet) {
                TypeUtils.setFieldValue(((HSSFSheet) poiSheet).getSheet(), "_dataValidityTable", null);
            }
        } catch (Exception e) {
            // do nothing
        }

        for (DataValidation dv : dataValidations) {
            CellRangeAddressList regions = dv.getRegions();
            for (int i = 0; i < regions.countRanges(); i++) {
                CellRangeAddress dvRegion = regions.getCellRangeAddress(i);
                if (dvRegion.getFirstRow() >= startRow) {
                    dvRegion.setFirstRow(dvRegion.getFirstRow() + rowsCount);
                }
                if (dvRegion.getLastRow() >= startRow) {
                    dvRegion.setLastRow(dvRegion.getLastRow() + rowsCount);
                }
            }
            poiSheet.addValidationData(poiSheet.getDataValidationHelper().createValidation(dv.getValidationConstraint(), dv.getRegions()));
        }
    }

    private class RowIterator implements Iterator<Row> {

        private org.apache.poi.ss.usermodel.Sheet poiSheet;
        private int index = 0;
        private int rowsCount;

        public RowIterator(org.apache.poi.ss.usermodel.Sheet poiSheet) {
            this.poiSheet = poiSheet;
            this.rowsCount = poiSheet.getLastRowNum() + 1;
        }

        @Override
        public boolean hasNext() {
            return index < rowsCount;
        }

        @Override
        public Row next() {
            int rowIndex = index++;
            org.apache.poi.ss.usermodel.Row nextRow = poiSheet.getRow(rowIndex);
            return nextRow != null ? new Row(Sheet.this, rowIndex) : null;
        }
    }
}
