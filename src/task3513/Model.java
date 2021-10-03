package task3513;

import java.util.*;
import java.util.stream.Collectors;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
    int score = 0;
    int maxTile = 0;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;

    public Model() {
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    void resetGameTiles() {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTiles = new ArrayList<>();
        emptyTiles = Arrays.stream(gameTiles).flatMap(Arrays::stream).filter(Tile::isEmpty).collect(Collectors.toList());
        return emptyTiles;
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles.isEmpty()) return;
        int randomTile = (int) (Math.random() * emptyTiles.size());
        emptyTiles.get(randomTile).value = (Math.random() < 0.9 ? 2 : 4);
    }

    private boolean compressTiles(Tile[] tiles) {
        int insertPosition = 0;
        boolean result = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()) {
                if (i != insertPosition) {
                    tiles[insertPosition] = tiles[i];
                    tiles[i] = new Tile();
                    result = true;
                }
                insertPosition++;
            }
        }
        return result;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean isMerged = false;
        for (int i = 0; i < tiles.length - 1; i++) {
            if (tiles[i].value == tiles[i + 1].value && tiles[i].value > 0) {
                int newValue = tiles[i].value * 2;
                tiles[i].value = newValue;
                if (newValue > maxTile) maxTile = newValue;
                score += newValue;
                tiles[i + 1].value = 0;
                i++;
                isMerged = true;
            }
        }
        compressTiles(tiles);
        return isMerged;
    }

    public void left() {
        if (isSaveNeeded) saveState(gameTiles);
        boolean isNeededToAdd = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i]))
                isNeededToAdd = true;
        }
        if (isNeededToAdd) addTile();
        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        gameTiles = rotateClockWise();
        gameTiles = rotateClockWise();
        left();
        gameTiles = rotateClockWise();
        gameTiles = rotateClockWise();
    }

    public void up() {
        saveState(gameTiles);
        gameTiles = rotateClockWise();
        gameTiles = rotateClockWise();
        gameTiles = rotateClockWise();
        left();
        gameTiles = rotateClockWise();
    }

    public Tile[][] rotateClockWise() {
        Tile[][] rotated = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                rotated[i][j] = gameTiles[FIELD_WIDTH - 1 - j][i];
            }
        }
        return rotated;
    }
    /*   public void right() {
        Tile[][] tilesToRight = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                tilesToRight[i][j] = gameTiles[i][FIELD_WIDTH - 1 - j];
            }
        }
        boolean isNeededToAdd = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(tilesToRight[i]) | mergeTiles(tilesToRight[i]))
                isNeededToAdd = true;
        }
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = tilesToRight[i][FIELD_WIDTH - 1 - j];
            }
        }
        if (isNeededToAdd) addTile();
     }
  */


    public void down() {
        saveState(gameTiles);
        gameTiles = rotateClockWise();
        left();
        gameTiles = rotateClockWise();
        gameTiles = rotateClockWise();
        gameTiles = rotateClockWise();
    }

    public boolean canMove() {
        if (getEmptyTiles().size() > 0) return true;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if ((i < FIELD_WIDTH - 1 && gameTiles[i][j].value == gameTiles[i + 1][j].value)
                        || ((j < FIELD_WIDTH - 1) && gameTiles[i][j].value == gameTiles[i][j + 1].value))
                    return true;
            }
        }
        return false;
    }

    private void saveState(Tile[][] currentGameTiles) {
        Tile[][] toSave = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                toSave[i][j] = new Tile(currentGameTiles[i][j].value);
            }
        }
        previousStates.push(toSave);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousScores.isEmpty() && !previousStates.isEmpty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public void randomMove() {
        switch (((int) (Math.random() * 100)) % 4) {
            case 0:
                up();
                break;
            case 1:
                right();
                break;
            case 2:
                down();
                break;
            case 3:
                left();
                break;
        }
    }
    private boolean hasBoardChanged() {
        Tile[][] lastTiles = previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (lastTiles[i][j].value != gameTiles[i][j].value)
                    return true;
            }
        }
        return false;
    }
    private MoveEfficiency getMoveEfficiency(Move move) {
        move.move();
        MoveEfficiency moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        if (!hasBoardChanged())
            return new MoveEfficiency(-1, 0, move);
        else {
            rollback();
            return moveEfficiency;
        }
    }
    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(() -> left()));
        queue.offer(getMoveEfficiency(() -> right()));
        queue.offer(getMoveEfficiency(() -> down()));
        queue.offer(getMoveEfficiency(() -> up()));
        queue.peek().getMove().move();
    }
}
