package ru.itits.models;

public class Cell {
    private int column;
    private int row;

    public Cell() {
    }

    public Cell(int column, int row) {
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    @Override
    public int hashCode() {
        return column + row;
    }

    @Override
    public boolean equals(Object obj) {
        Cell cell = (Cell) obj;

        return (this.getColumn()==cell.getColumn())&&(this.getRow()==cell.getRow());
    }
}
