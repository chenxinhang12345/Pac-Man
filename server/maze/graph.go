package maze

type Cell struct {
	Top    bool
	Bottom bool
	Left   bool
	Right  bool
	Row    int
	Col    int
}

type Edge struct {
	Cell1 Cell
	Cell2 Cell
	Pos   POS
}

type POS int

const (
	T      POS = 0
	B      POS = 1
	L      POS = 2
	R      POS = 3
	width  int = 6
	height int = 6
)

type Maze struct {
	Cells []Cell
	Edges []Edge
}

func NewCell(row, col int) Cell {
	return Cell{true, true, true, true, row, col}
}

func NewEdge(cell1, cell2 Cell, pos POS) Edge {
	return Edge{cell1, cell2, pos}
}

func NewMaze() (m Maze) {
	for row := 0; row < height; row++ {
		for col := 0; col < width; col++ {
			m.AppendCell(NewCell(row, col))
		}
	}
	return
}

func (m Maze) AppendCell(cell Cell) {
	m.Cells = append(m.Cells, cell)
}
