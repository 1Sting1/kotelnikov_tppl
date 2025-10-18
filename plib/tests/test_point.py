import pytest
import json
from plib import Point

@pytest.fixture
def points():
    return Point(0, 0), Point(2, 2)

class TestPoint:
    def test_creation(self):
        p = Point(1,2)
        assert p.x == 1
        assert p.y == 2

        with pytest.raises(TypeError):
            Point(1.5, 1.5)

    def test_add_fixture(self, points):
        p1, p2 = points
        result = p1 + p2
        assert result.x == 2
        assert result.y == 2

    def test_add(self):
        p1 = Point(0,0)
        p2 = Point(2,2)
        assert p1 + p2 == Point(2,2)

    def test_sub(self):
        p1 = Point(0,0)
        p2 = Point(2,2)
        assert p2 - p1 == Point(2,2)
        assert p1 - p2 == Point(-2,-2)

    def test_distance_to(self):
        p1 = Point(0,0)
        p2 = Point(2,0)
        assert p1.to(p2) == 2

    @pytest.mark.parametrize(
        'p1,p2,distance',
        [(Point(0,0), Point(0,10), 10),
        (Point(0,0), Point(10,0), 10),
        (Point(0,0), Point(1,1), 1.414),]
    )
    def test_distance_all_axis(self, p1, p2, distance):
        assert p1.to(p2) == pytest.approx(distance, 0.001)

    def test_str(self):
        p = Point(1, 2)
        assert str(p) == "(Point,1 2)"

    def test_repr(self):
        p = Point(1, 2)
        assert repr(p) == "(Point,1 2)"

    def test_is_center(self):
        p1 = Point(0, 0)
        p2 = Point(1, 2)
        assert p1.is_center() is True
        assert p2.is_center() is False

    def test_to_json(self):
        p = Point(1, 2)
        assert p.to_json() == json.dumps({'x': 1, 'y': 2})

    def test_from_json(self):
        s = '{"x": 1, "y": 2}'
        p = Point.from_json(s)
        assert p.x == 1
        assert p.y == 2

    def test_iadd(self):
        p1 = Point(1, 1)
        p2 = Point(2, 2)
        p1 += p2
        assert p1 == Point(3, 3)

    def test_eq(self):
        p1 = Point(1, 2)
        p2 = Point(1, 2)
        p3 = Point(3, 4)
        assert p1 == p2
        assert p1 != p3

    def test_eq_not_implemented(self):
        p1 = Point(1, 2)
        with pytest.raises(NotImplementedError):
            p1 == 123

    def test_neg(self):
        p1 = Point(1, 2)
        p2 = -p1
        assert p2.x == -1
        assert p2.y == -2