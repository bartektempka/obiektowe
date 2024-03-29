package agh.ics.oop;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.System.out;

class Vector2d {
    public final int x;
    public final int y;

    public Vector2d(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return '(' + Integer.toString(this.x) + "," + this.y + ")";
    }

    public boolean precedes(Vector2d other) {
        return this.x <= other.x && this.y <= other.y;
    }

    public boolean follows(Vector2d other) {
        return this.x >= other.x && this.y >= other.y;
    }

    public Vector2d add(Vector2d other) {
        return new Vector2d(this.x + other.x, this.y + other.y);
    }

    public Vector2d subtract(Vector2d other) {
        return new Vector2d(this.x - other.x, this.y - other.y);
    }

    public Vector2d upperRight(Vector2d other) {
        return new Vector2d(Math.max(this.x, other.x), Math.max(this.y, other.y));
    }

    public Vector2d lowerLeft(Vector2d other) {
        return new Vector2d(Math.min(this.x, other.x), Math.min(this.y, other.y));
    }

    public Vector2d opposite() {
        return new Vector2d(this.x * -1, this.y * -1);
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Vector2d that))
            return false;
        return this.x == that.x && this.y == that.y;
    }

    public int hashCode() {
        return Objects.hash(this.x,this.y);
    }
}

enum MoveDirection {
    FORWARD, BACKWARD, RIGHT, LEFT, DEFAULT
}

enum MapDirections {
    NORTH, SOUTH, WEST, EAST;

    public String toString() {
        return switch (this) {
            case EAST -> "East";
            case WEST -> "West";
            case NORTH -> "North";
            case SOUTH -> "South";
        };
    }

    public MapDirections next() {
        return switch (this) {
            case EAST -> SOUTH;
            case WEST -> NORTH;
            case NORTH -> EAST;
            case SOUTH -> WEST;
        };
    }

    public MapDirections previous() {
        return switch (this) {
            case EAST -> NORTH;
            case WEST -> SOUTH;
            case NORTH -> WEST;
            case SOUTH -> EAST;
        };
    }

    public Vector2d toUnitVector() {
        return switch (this) {
            case EAST -> new Vector2d(1, 0);
            case WEST -> new Vector2d(-1, 0);
            case NORTH -> new Vector2d(0, 1);
            case SOUTH -> new Vector2d(0, -1);
        };
    }
}

class Animal implements IMapElement {
    private MapDirections orientation = MapDirections.NORTH;
    private Vector2d position;
    private final AbstractWorldMap animalMap;
    private final ArrayList<IPositionChangeObserver> observers = new ArrayList<IPositionChangeObserver>();

    public Animal() {
        this.position = new Vector2d(2, 2);
        this.animalMap = new RectangularMap(4, 4);
    }

    public Animal(AbstractWorldMap map) {
        this.position = new Vector2d(2, 2);
        this.animalMap = map;
        addObserver(map);

    }

    public Animal(AbstractWorldMap map, Vector2d initialPosition) {
        this.position = initialPosition;
        this.animalMap = map;
        addObserver(map);
    }

    public Vector2d getPosition() {
        return this.position;
    }

    public String toString() {
        String a = switch (this.orientation) {
            case WEST -> "W";
            case EAST -> "E";
            case NORTH -> "N";
            case SOUTH -> "S";
        };
        return a + ' ' + this.position.toString();
    }

    public boolean isAt(Vector2d positionToCheck) {
        return this.position.equals(positionToCheck);
    }

    public void move(MoveDirection direction) {
        Vector2d newPosition = this.orientation.toUnitVector();
        switch (direction) {
            case LEFT:
                this.orientation = this.orientation.previous();
                return;
            case RIGHT:
                this.orientation = this.orientation.next();
                return;
            case FORWARD:
                newPosition = this.position.add(newPosition);
                break;
            case BACKWARD:
                newPosition = this.position.subtract(newPosition);
                break;
            case DEFAULT:
                return;
        }
        if (this.animalMap.canMoveTo(newPosition)) {
            positionChanged(this.position,newPosition);
            this.position = newPosition;
        }
    }
    public void addObserver(IPositionChangeObserver observer){
        this.observers.add(observer);
    }
    public void removeObserver(IPositionChangeObserver observer){
        this.observers.remove(observer);
    }
    public void positionChanged(Vector2d oldPosition,Vector2d newPosition){
        for(IPositionChangeObserver observer : observers){
            observer.positionChanged(oldPosition,newPosition);
        }
    }
}


public class World {
    public static void main(String[] args) {
        MoveDirection[] directions = new OptionsParser().parse(args);
        AbstractWorldMap map = new RectangularMap(10, 5);
        Vector2d[] positions = {new Vector2d(2, 2), new Vector2d(3, 4)};
        IEngine engine = new SimulationEngine(directions, map, positions);
        engine.run();
    }

    public static void run(Directions[] dir) {
        for (Directions direction : dir) {
            switch (direction) {
                case f -> out.println("Zwierzak idzie do przodu");
                case b -> out.println("Zwierzak idzie do tyłu");
                case r -> out.println("Zwierzak skręca w prawo");
                case l -> out.println("Zwierzak skręca w lewo");
            }
        }
    }

    public static Directions[] to_enum(String[] dir) {
        Directions[] enum_dir = new Directions[dir.length];
        for (int i = 0; i < dir.length; i++) {
            Directions x = switch (dir[i]) {
                case "f" -> Directions.f;
                case "b" -> Directions.b;
                case "l" -> Directions.l;
                case "r" -> Directions.r;
                default -> Directions.nan;
            };
            enum_dir[i] = x;
        }
        return enum_dir;
    }
}
