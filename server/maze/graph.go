package maze

import (
	"encoding/json"
	"math/rand"
	"time"

	"github.com/sirupsen/logrus"
)

// Cell defines the cell in the maze
type Cell struct {
	Top    bool
	Bottom bool
	Left   bool
	Right  bool
	Row    int
	Col    int
}

// Edge connects two edges, which actually is a wall
type Edge struct {
	Cell1 *Cell
	Cell2 *Cell
	Pos   POS
}

// POS defines the position of the wall
type POS int

const (
	// T is the top wall
	T POS = 0
	// B is the bottom wall
	B POS = 1
	// L is the left wall
	L POS = 2
	// R is the right wall
	R POS = 3
	// Width is the number of cells on the row
	Width int = 40
	// Height is the number of cells on the col
	Height int = 40
	// MazeHeight is the pixel length of the col
	MazeHeight int = 1500
	// MazeWidth is the pixel length of the row
	MazeWidth int = 1300
)

// Maze is the main data structure to store the maze
type Maze struct {
	Cells   [][]*Cell
	Edges   []Edge
	CellSet *DSet
}

// NewCell is to create a new cell in the maze based on given coordinates
// By default, each wall exists
func NewCell(row, col int) *Cell {
	return &Cell{true, true, true, true, row, col}
}

// NewEdge is to create an edge between two cells
func NewEdge(cell1, cell2 *Cell, pos POS) Edge {
	return Edge{cell1, cell2, pos}
}

// NewMaze is to create a new maze
// By default, each wall is capsulated
func NewMaze() *Maze {
	m := new(Maze)
	m.Cells = make([][]*Cell, Height)
	for row := 0; row < Height; row++ {
		m.Cells[row] = make([]*Cell, Width)
	}
	for row := 0; row < Height; row++ {
		for col := 0; col < Width; col++ {
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
	m.CellSet = NewDSet(m.Cells)
	return m
}

// AppendCell is to add a new cell to the maze
func (m *Maze) AppendCell(cell *Cell) {
	m.Cells[cell.Row][cell.Col] = cell
}

// AddEdge is to add a new edge in the maze
func (m *Maze) AddEdge(edge Edge) {
	m.Edges = append(m.Edges, edge)
}

// SetUp is to run randomized Kruskal's algorithm to generate a new maze
func (m *Maze) SetUp() {
	cellSet := NewDSet(m.Cells)
	rand.NewSource(int64(time.Now().Nanosecond()))
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

// FindCellByCoord is a GET method to get the cell pointer based on coord
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

// ToBytes is to serialize the struct of maze
func (m *Maze) ToBytes() []byte {
	widthPart := MazeWidth / Width
	heightPart := MazeHeight / Height
	type coord struct {
		X0 int
		X1 int
		Y0 int
		Y1 int
	}
	// Collect all existed walls in the maze
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

// ToString is to convert the serialized maze to string
func (m *Maze) ToString() string {
	return string(m.ToBytes())
}
