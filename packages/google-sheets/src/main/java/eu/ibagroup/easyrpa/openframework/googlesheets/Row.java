package eu.ibagroup.easyrpa.openframework.googlesheets;


import com.google.api.services.sheets.v4.model.*;
import eu.ibagroup.easyrpa.openframework.googlesheets.internal.GSheetElementsCache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Row implements Iterable<Cell> {

    private String id;

    private Sheet parent;

    private String documentId;

    private int sheetIndex;

    private int rowIndex;

    private List<Request> requests = new ArrayList<>();

    protected Row(Sheet parent, int rowIndex) {
        this.parent = parent;
        this.documentId = parent.getDocument().getId();
        this.sheetIndex = parent.getIndex();
        this.rowIndex = rowIndex;
        this.id = this.sheetIndex + "|" + this.rowIndex;
    }

    public SpreadsheetDocument getDocument() {
        return parent.getDocument();
    }

    public Sheet getSheet() {
        return parent;
    }

    public int getIndex() {
        return rowIndex;
    }

    public CellRef getReference() {
        return new CellRef(rowIndex, 0);
    }

    public Object getValue(String cellRef) {
        return getValue(new CellRef(cellRef), Object.class);
    }

    public <T> T getValue(String cellRef, Class<T> valueType) {
        return getValue(new CellRef(cellRef), valueType);
    }

    public Object getValue(CellRef cellRef) {
        return getValue(cellRef, Object.class);
    }

    public <T> T getValue(CellRef cellRef, Class<T> valueType) {
        return cellRef != null ? getValue(cellRef.getCol(), valueType) : null;
    }

    public Object getValue(int colIndex) {
        return getValue(colIndex, Object.class);
    }

    public <T> T getValue(int colIndex, Class<T> valueType) {
        Cell cell = getCell(colIndex);
        return cell != null ? cell.getValue(valueType) : null;
    }

    public void setValue(String cellRef, Object value) {
        setValue(new CellRef(cellRef), value);
    }

    public void setValue(CellRef cellRef, Object value) {
        if (cellRef != null) {
            setValue(cellRef.getCol(), value);
        }
    }

    public void setValue(int colIndex, Object value) {
        Cell cell = getCell(colIndex);
        if (cell == null) {
            cell = createCell(colIndex);
        }
        cell.setValue(value);
    }

    public List<Request> setValuesInOneTransaction(List<?> rowDataList) {
        String sessionId = getDocument().generateNewSessionId();
        getDocument().openSessionIfRequired(sessionId);
        if (rowDataList != null && !rowDataList.isEmpty()) {
            int i = 0;
            for (Object currentCellValue : rowDataList) {
                Cell cell = new Cell(parent, rowIndex, this.getFirstCellIndex()+i);
                requests.addAll(cell.setValue(currentCellValue));
                i++;
            }
        }
        getDocument().closeSessionIfRequired(sessionId, requests);
        return requests;
    }

    public List<Request> setFormulasInOneTransaction(List<String> rowDataList) {
        String sessionId = getDocument().generateNewSessionId();
        getDocument().openSessionIfRequired(sessionId);
        if (rowDataList != null && !rowDataList.isEmpty()) {
            int i = 0;
            for (String currentCellValue : rowDataList) {
                Cell cell = new Cell(parent, rowIndex, this.getFirstCellIndex()+i);
                requests.addAll(cell.setFormula(currentCellValue));
                i++;
            }
        }
        getDocument().closeSessionIfRequired(sessionId, requests);
        return requests;
    }

    public List<Request> setStylesInOneTransaction(List<GSheetCellStyle> rowDataList) {
        String sessionId = getDocument().generateNewSessionId();
        getDocument().openSessionIfRequired(sessionId);
        if (rowDataList != null && !rowDataList.isEmpty()) {
            int i = 0;
            for (GSheetCellStyle currentCellValue : rowDataList) {
                Cell cell = new Cell(parent, rowIndex, this.getFirstCellIndex()+i);
                requests.addAll(cell.setStyle(currentCellValue));
                i++;
            }
        }
        getDocument().closeSessionIfRequired(sessionId, requests);
        return requests;
    }

    public void setValue(List<RowData> rowDataList) {
        String sessionId = getDocument().generateNewSessionId();
        getDocument().openSessionIfRequired(sessionId);
        if (rowDataList != null && !rowDataList.isEmpty()) {
            requests.add(new Request().setUpdateCells(new UpdateCellsRequest()
                    .setRange(new GridRange()
                            .setSheetId(getDocument().getActiveSheet().getId())
                            .setStartRowIndex(this.rowIndex)
                            .setEndRowIndex(this.rowIndex + 1)
                            .setStartColumnIndex(this.getFirstCellIndex())
                            .setEndColumnIndex(this.getLastCellIndex())
                    )
                    .setRows(rowDataList)
                    .setFields("userEnteredValue")));
        }
        getDocument().closeSessionIfRequired(sessionId, requests);
    }

    public List<Object> getValues() {
        return getValues(Object.class);
    }

    public <T> List<T> getValues(Class<T> valueType) {
        return getRange(getFirstCellIndex(), getLastCellIndex(), valueType);
    }

    public void setValues(List<?> values) {
        putRange(0, values);
    }

    public List<Object> getRange(String startRef, String endRef) {
        return getRange(new CellRef(startRef), new CellRef(endRef));
    }

    public <T> List<T> getRange(String startRef, String endRef, Class<T> valueType) {
        return getRange(new CellRef(startRef), new CellRef(endRef), valueType);
    }

    public List<Object> getRange(CellRef startRef, CellRef endRef) {
        return getRange(startRef, endRef, Object.class);
    }

    public <T> List<T> getRange(CellRef startRef, CellRef endRef, Class<T> valueType) {
        return startRef != null && endRef != null ? getRange(startRef.getCol(), endRef.getCol(), valueType) : null;
    }

    public List<Object> getRange(int startCol, int endCol) {
        return getRange(startCol, endCol, Object.class);
    }

    public <T> List<T> getRange(int startCol, int endCol, Class<T> valueType) {
        List<T> values = new ArrayList<>();

        int c1 = Math.min(startCol, endCol);
        int c2 = Math.max(startCol, endCol);

        for (int col = c1; col <= c2; col++) {
            values.add(getValue(col, valueType));
        }
        return values;
    }

    public void putRange(String startRef, List<?> data) {
        putRange(new CellRef(startRef), data);
    }

    public void putRange(CellRef startRef, List<?> data) {
        if (startRef != null) {
            putRange(startRef.getCol(), data);
        }
    }

    public void putRange(int startCol, List<?> data) {
        if (data != null) {
            int col = startCol;
            for (Object cellValue : data) {
                setValue(col++, cellValue);
            }
        }
    }

    public Cell getCell(String cellRef) {
        return getCell(new CellRef(cellRef));
    }

    public Cell getCell(CellRef cellRef) {
        return cellRef != null ? getCell(cellRef.getCol()) : null;
    }

    public Cell getCell(int colIndex) {
        if (colIndex >= 0 && colIndex < getGSheetRow().getValues().size()) {
            CellData cell = getGSheetRow().getValues().get(colIndex);
            return cell != null ? new Cell(parent, rowIndex, colIndex) : null;
        }
        return null;
    }

    public Cell createCell(int colIndex) {
        Cell cell = new Cell(parent, rowIndex, colIndex);
        getGSheetRow().getValues().add(colIndex, cell.getGCell());
        return cell;
    }

    public Cell addCell(Object value) {
        Cell cell = createCell(getLastCellIndex() + 1);
        cell.setValue(value);
        return cell;
    }

    public int getFirstCellIndex() {
        List<CellData> row = getGSheetRow().getValues();
        for(int i=0; i<row.size();i++){
            if(row.get(i).getUserEnteredValue()!=null){
                return i;
            }
        }
        return 0;
    }

    public int getLastCellIndex() {
        return getGSheetRow().getValues().size() - 1;
    }

    @Override
    public Iterator<Cell> iterator() {
        return new CellIterator(getGSheetRow());
    }

    public RowData getGSheetRow() {
        return GSheetElementsCache.getGRow(documentId, id, sheetIndex, rowIndex);
    }

    private class CellIterator implements Iterator<Cell> {

        private RowData gSheetRow;
        private int index = 0;
        private int cellsCount;

        public CellIterator(RowData gSheetRow) {
            this.gSheetRow = gSheetRow;
            this.cellsCount = gSheetRow.getValues().size();
        }

        @Override
        public boolean hasNext() {
            if (index < cellsCount) {
                CellData nextCell = gSheetRow.getValues().get(index);
                while (nextCell == null && index + 1 < cellsCount) {
                    nextCell = gSheetRow.getValues().get(++index);
                }
                return nextCell != null;
            }
            return false;
        }

        @Override
        public Cell next() {
            return new Cell(parent, rowIndex, index++);
        }
    }
}
