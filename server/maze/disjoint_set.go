package maze

// Set is the element of the disjoint set
type Set struct {
	data   *Cell
	parent *Set
}

// DSet contains the sets
type DSet struct {
	setList []*Set
}

// Find is to find the root of the element/set
func (s *Set) Find() *Set {
	if s.parent == s {
		return s
	}
	// Path compression
	s.parent = s.parent.Find()
	return s.parent
}

// Union is to add a set to another set
func (s *Set) Union(otherSet Set) {
	p1 := s.Find()
	p2 := otherSet.Find()
	p1.parent = p2
}

// Equal is to compare two cells
func (s *Set) Equal(data *Cell) bool {
	if s.data == data {
		return true
	}
	return false
}

// Find is to get the root of the given cell
func (ds *DSet) Find(cell *Cell) *Set {
	for _, set := range ds.setList {
		if set.Equal(cell) {
			return set.Find()
		}
	}
	return nil
}

// Union is to union two cells
func (ds *DSet) Union(cell1, cell2 *Cell) {
	var set1 *Set
	var set2 *Set
	for _, set := range ds.setList {
		if set.Equal(cell1) {
			set1 = set
		}
		if set.Equal(cell2) {
			set2 = set
		}
	}
	set1.Union(*set2)
}

// Append is to append a new set to the collection
func (ds *DSet) Append(set *Set) {
	ds.setList = append(ds.setList, set)
}

// Size is to calculate how many distinct sets in the collection
func (ds *DSet) Size() int {
	count := 0
	for _, set := range ds.setList {
		if *set.parent == *set {
			count++
		}
	}
	return count
}

// NewSet is to create new set/element
func NewSet(cell *Cell) *Set {
	set := &Set{
		data: cell,
	}
	set.parent = set
	return set
}

// NewDSet is to create a new set collection
func NewDSet(cells [][]*Cell) *DSet {
	ds := new(DSet)
	for _, rows := range cells {
		for _, cell := range rows {
			ds.Append(NewSet(cell))
		}
	}
	return ds
}
