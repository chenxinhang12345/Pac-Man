package maze

type Set struct {
	data   Cell
	parent *Set
}

type DSet struct {
	setList []Set
}

func (s Set) Find() *Set {
	if *s.parent == s {
		return &s
	}
	s.parent = s.parent.Find()
	return s.parent
}

func (s Set) ChangeParent(parent *Set) {
	s.parent = parent
}

func (s Set) Union(otherSet Set) {
	p1 := s.Find()
	p2 := otherSet.Find()
	p1.ChangeParent(p2)
}

func (s Set) Equal(data Cell) bool {
	if s.data == data {
		return true
	}
	return false
}

func (ds DSet) Find(cell Cell) *Set {
	for _, set := range ds.setList {
		if set.Equal(cell) {
			return set.Find()
		}
	}
	return nil
}

func (ds DSet) Union(cell1, cell2 Cell) {
	var set1 Set
	var set2 Set
	for _, set := range ds.setList {
		if set.Equal(cell1) {
			set1 = set
		}
		if set.Equal(cell2) {
			set2 = set
		}
	}
	set1.Union(set2)
}

func (ds DSet) Append(set Set) {
	ds.setList = append(ds.setList, set)
}

func (ds DSet) Size() int {
	count := 0
	for _, set := range ds.setList {
		if *set.parent == set {
			count++
		}
	}
	return count
}

func NewSet(cell Cell) Set {
	set := Set{
		data: cell,
	}
	set.parent = &set
	return set
}

func NewDSet(cells [][]Cell) DSet {
	ds := DSet{}
	for _, rows := range cells {
		for _, cell := range rows {
			ds.Append(NewSet(cell))

		}
	}
	return ds
}
