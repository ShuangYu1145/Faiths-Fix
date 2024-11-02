package com.github.stachelbeere1248.zombiesutils.timer;

import com.github.stachelbeere1248.zombiesutils.game.enums.Map;
import com.github.stachelbeere1248.zombiesutils.utils.InvalidMapException;
import com.github.stachelbeere1248.zombiesutils.utils.ScoardboardException;
import com.github.stachelbeere1248.zombiesutils.utils.Scoreboard;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class GameManager {
    private final HashMap<String, Game> GAMES;

    public GameManager() {
        GAMES = new HashMap<>();
    }

    public Optional<Game> getGame() {
        return Scoreboard.getServerNumber().map(GAMES::get);
    }

    public void endGame(final String serverNumber, final boolean isWin) {
        if (!GAMES.containsKey(serverNumber)) return;
        final Game game = GAMES.get(serverNumber);
        if (isWin) {
            switch (game.getGameMode().getMap()) {
                    case DEAD_END:
                    case BAD_BLOOD:
                    case PRISON:
                        game.pass(30);
                        break;
                    case ALIEN_ARCADIUM:
                        game.pass(105);
                        break;
            }
        }
        GAMES.remove(serverNumber);

    }

    public void splitOrNew(int round) throws ScoardboardException, InvalidMapException {
        final String serverNumber = Scoreboard.getServerNumber().orElseThrow(ScoardboardException::new);
        if (GAMES.containsKey(serverNumber)) {
            if (round == 0) GAMES.put(serverNumber, new Game(Map.getMap().orElseThrow(InvalidMapException::new), serverNumber));
            else GAMES.get(serverNumber).pass(round);
        } else {
            GAMES.put(serverNumber, new Game(Map.getMap().orElseThrow(InvalidMapException::new), serverNumber, round + 1));
        }
    }

    public Set<String> getGames() {
        return this.GAMES.keySet();
    }

    public void killAll() {
        GAMES.clear();
    }

}
