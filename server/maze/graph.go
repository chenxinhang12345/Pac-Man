package maze

import (
	"encoding/json"
	"math/rand"

	"github.com/sirupsen/logrus"
)

type Cell struct {
	Top    bool
	Bottom bool
	Left   bool
	Right  bool
	Row    int
	Col    int
}

type Edge struct {
	Cell1 *Cell
	Cell2 *Cell
	Pos   POS
}

type POS int

const (
	T          POS = 0
	B          POS = 1
	L          POS = 2
	R          POS = 3
	width      int = 6
	height     int = 6
	MazeHeight int = 1500
	MazeWidth  int = 1300
)

type Maze struct {
	Cells [][]*Cell
	Edges []Edge
}

func NewCell(row, col int) *Cell {
	return &Cell{true, true, true, true, row, col}
}

func NewEdge(cell1, cell2 *Cell, pos POS) Edge {
	return Edge{cell1, cell2, pos}
}

func NewMaze() *Maze {
	m := new(Maze)
	m.Cells = make([][]*Cell, height)
	for row := 0; row < height; row++ {
		m.Cells[row] = make([]*Cell, width)
	}
	for row := 0; row < height; row++ {
		for col := 0; col < width; col++ {
			m.AppendCell(NewCell(row, col))
		}
	}
	for _, rows := range m.Cells {
		for _, cell1 := range rows {
			for _, cols := range m.Cells {
				for _, cell2 := range cols {
					m.AddEdge(NewEdge(cell1, cell2, T))
					m.AddEdge(NewEdge(cell1, cell2, L))
				}
			}
		}
	}
	return m
}

func (m *Maze) AppendCell(cell *Cell) {
	m.Cells[cell.Row][cell.Col] = cell
}

func (m *Maze) AddEdge(edge Edge) {
	m.Edges = append(m.Edges, edge)
}

func (m *Maze) SetUp() {
	cellSet := NewDSet(m.Cells)
	for cellSet.Size() != 1 {
		// Remove random edge
		edgeN := rand.Intn(len(m.Edges))
		wall := m.Edges[edgeN]
		m.Edges = append(m.Edges[:edgeN], m.Edges[edgeN+1:]...)
		row := wall.Cell1.Row
		col := wall.Cell1.Col
		cell1 := m.FindCellByCoord(row, col)
		cell2 := m.FindCellByCoord(row, col-1)
		// Check whether the cells are in the same connected component
		// If not connect them
		if cell2 != nil && col > 0 && wall.Pos == L && cellSet.Find(cell1) != cellSet.Find(cell2) {
			cellSet.Union(cell1, cell2)
			cell1.Left = false
			cell2.Right = false
		}

		cell2 = m.FindCellByCoord(row-1, col)
		// Check whether the cells are in the same connected component
		// If not connect them
		if cell2 != nil && row > 0 && wall.Pos == T && cellSet.Find(cell1) != cellSet.Find(cell2) {
			cellSet.Union(cell1, cell2)
			cell1.Top = false
			cell2.Bottom = false
		}
	}
}

func (m *Maze) FindCellByCoord(row, col int) *Cell {
	for x, rows := range m.Cells {
		for y, cell := range rows {
			if x == row && y == col {
				return cell
			}
		}
	}
	return nil
}

func (m *Maze) ToBytes() []byte {
	widthPart := MazeWidth / width
	heightPart := MazeHeight / height
	type coord struct {
		X0 int
		X1 int
		Y0 int
		Y1 int
	}
	collectRows := make([]coord, 1)
	collectCols := make([]coord, 1)
	for row, rows := range m.Cells {
		for col, cell := range rows {
			if cell.Top == true {
				x0 := col * widthPart
				x1 := (col + 1) * widthPart
				y0 := row * heightPart
				y1 := row * heightPart
				collectRows = append(collectRows, coord{x0, x1, y0, y1})
			}
			if cell.Bottom == true {
				x0 := col * widthPart
				x1 := (col + 1) * widthPart
				y0 := (row + 1) * heightPart
				y1 := (row + 1) * heightPart
				collectRows = append(collectRows, coord{x0, x1, y0, y1})
			}
			if cell.Left == true {
				x0 := col * widthPart
				x1 := col * widthPart
				y0 := row * heightPart
				y1 := (row + 1) * heightPart
				collectCols = append(collectCols, coord{x0, x1, y0, y1})
			}
			if cell.Right == true {
				x0 := (col + 1) * widthPart
				x1 := (col + 1) * widthPart
				y0 := row * heightPart
				y1 := (row + 1) * heightPart
				collectCols = append(collectCols, coord{x0, x1, y0, y1})
			}
		}
	}
	mazeInfo := struct {
		Rows []coord
		Cols []coord
	}{
		collectRows,
		collectCols,
	}
	bytes, err := json.Marshal(mazeInfo)
	if err != nil {
		logrus.Error(err)
	}
	return bytes
}

func (m *Maze) ToString() string {
	return string(m.ToBytes())
}
