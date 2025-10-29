package com.wlf.app.main.io;

import com.wlf.app.main.data.Game;
import org.apache.commons.exec.*;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Logger;

public class GameHandler {
    private static final Logger log = Logger.getLogger(GameHandler.class.getSimpleName());

    public static DefaultExecuteResultHandler startGameExe(Game game) throws IOException {
        String path;

        path = game.getGameFolder() + "/Bin/SeriousSam.exe";

        CommandLine cmdLine = new CommandLine(path);

        cmdLine.addArgument("+game");
        cmdLine.addArgument("SeriousContentManager");

        log.info("Running: " + cmdLine);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        startGame(cmdLine, resultHandler);

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        return resultHandler;
    }

    public static DefaultExecuteResultHandler startGameWithSteam(Game game) throws IOException {
        String path;
        String gameid;
        path = "steam";

        if (game == Game.TFE) {
            gameid = "steam://rungameid/41050//+game SeriousContentManager";
        } else {
            gameid = "steam://rungameid/41060//+game SeriousContentManager";
        }

        CommandLine cmdLine = new CommandLine(path);
        if (true /*config.isUseSteamRuntime()*/) {
            cmdLine.addArgument(gameid, false);
        }
        log.info("Running: " + cmdLine);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        startGame(cmdLine, resultHandler);

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        return resultHandler;
    }

    private static void startGame(CommandLine cmdLine, DefaultExecuteResultHandler resultHandler) throws IOException {
        ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(ExecuteWatchdog.INFINITE_TIMEOUT_DURATION).get();
        Executor executor = DefaultExecutor.builder().get();
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);
        executor.execute(cmdLine, resultHandler);
    }
}
