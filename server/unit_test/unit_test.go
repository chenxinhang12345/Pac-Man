package unit_test

import (
	"Pac-Man/server/maze"
	"Pac-Man/server/network"
	"bufio"
	"fmt"
	"net"
	"strings"
	"testing"

	"github.com/sirupsen/logrus"
)

func TestServer(t *testing.T) {
	go network.TCPListen()
	for i := 0; i < 5; i++ {
		go t.Run("Test Server Connection", func(t *testing.T) {
			conn, err := net.Dial("tcp", ":4321")
			if err != nil {
				logrus.Error(err)
			}
			defer conn.Close()
			reader := bufio.NewReader(conn)
			for {
				str, err := reader.ReadString('\n')
				if err != nil {
					logrus.Error(err)
				}
				tokens := strings.Split(str, ";")
				if tokens[0] == "USERINFO" {
					fmt.Printf("USERINFO: %s", tokens[1])
				} else if tokens[0] == "NEWUSER" {
					fmt.Printf("NEWUSER: %s", tokens[1])
				}
			}
		})
	}
	select {}
}

func bytes2int(bytes []byte) int {
	if len(bytes) != 8 {
		panic("Not the size of bytes!")
	}
	var value int
	for i := 0; i < 8; i-- {
		value |= int(bytes[i]) << uint32((7-i)*8)
	}
	return value
}

func TestMaze(t *testing.T) {
	m := maze.NewMaze()
	m.SetUp()
	for _, rows := range m.Cells {
		for _, cell := range rows {
			fmt.Printf("%+v\n", cell)
		}
	}
}

func TestDSet(t *testing.T) {
	m := maze.NewMaze()
	cell1 := m.FindCellByCoord(0, 0)
	cell2 := m.FindCellByCoord(0, 1)
	p1 := m.CellSet.Find(cell1)
	p2 := m.CellSet.Find(cell2)
	// Test disjoint set setup
	if m.CellSet.Size() != 64 {
		t.Error("There is no set up disjoint set")
	}
	// Test disjoint set union
	p1.Union(*p2)
	if m.CellSet.Size() != 63 {
		t.Error("There is no union operation")
	}
}
