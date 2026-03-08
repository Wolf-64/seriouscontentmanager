package com.wlf.app.main.io;

import com.wlf.app.main.data.Game;
import com.wlf.app.preferences.Config;
import com.wlf.common.util.OSUtil;
import com.wlf.common.util.WindowsRegistry;
import org.apache.commons.exec.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

public class GameHandler {
    private static final Logger log = Logger.getLogger(GameHandler.class.getSimpleName());

    private static final String NAME_TEMP_MOD = "SeriousContentManager";
    private static final String GAMEID_TFE = "steam://rungameid/41050//";
    private static final String GAMEID_TSE = "steam://rungameid/41060//";
    private static final String WIN_REG_STEAM_64 = "HKLM\\SOFTWARE\\WOW6432Node\\Valve\\Steam";
    private static final String WIN_REG_STEAM_32 = "HKLM\\SOFTWARE\\Valve\\Steam";

    private static String regValueCache;

    public static String getGamePath(Game game) {
        if (game == Game.TFE) {
            return Config.getInstance().getDirectoryTFE();
        } else if (game == Game.TSE) {
            return Config.getInstance().getDirectoryTSE();
        } else {
            return null;
        }
    }

    public static DefaultExecuteResultHandler startGame(Game game) throws IOException {
        return startGame(game, NAME_TEMP_MOD);
    }

    public static DefaultExecuteResultHandler startGame(Game game, String modName) throws IOException {
        if (Config.getInstance().isUseSteamRuntime()) {
            return runSteamExecutable(game, modName);
        } else {
            return runGameExe(game, modName);
        }
    }

    private static DefaultExecuteResultHandler runGameExe(Game game, String modName) throws IOException {
        Path path = Path.of(Objects.requireNonNull(game.getGameFolder()), "Bin", "SeriousSam.exe");

        CommandLine cmdLine = new CommandLine(path.toFile());

        cmdLine.addArgument("+game");
        cmdLine.addArgument(modName);

        log.info("Running: " + cmdLine);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        runExecutable(cmdLine, resultHandler);

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        return resultHandler;
    }

    public static DefaultExecuteResultHandler runSteamExecutable(Game game, String modname) throws IOException {
        Path path;
        String gameId;

        switch (OSUtil.getOS()) {
            case LINUX -> path = Path.of("steam");
            case WIN -> {
                if (regValueCache == null) {
                    regValueCache = Path.of(Objects.requireNonNull(WindowsRegistry.readRegistry(WIN_REG_STEAM_64, "InstallPath")), "Steam.exe").toString();
                }
                path = Path.of(regValueCache);
            }
            default -> path = null;
        }

        if (game == Game.TFE) {
            gameId = GAMEID_TFE;
        } else {
            gameId = GAMEID_TSE;
        }

        String fullGameArgument = gameId + "+game " + modname;

        CommandLine cmdLine = new CommandLine(path.toFile());
        cmdLine.addArgument(fullGameArgument, false);

        log.info("Running: " + cmdLine);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        runExecutable(cmdLine, resultHandler);

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        return resultHandler;
    }

    private static void runExecutable(CommandLine cmdLine, DefaultExecuteResultHandler resultHandler) throws IOException {
        ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(ExecuteWatchdog.INFINITE_TIMEOUT_DURATION).get();
        Executor executor = DefaultExecutor.builder().get();
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);
        executor.execute(cmdLine, resultHandler);
    }
}
