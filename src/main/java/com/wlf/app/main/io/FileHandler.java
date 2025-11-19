package com.wlf.app.main.io;

import com.wlf.app.main.data.*;
import com.wlf.app.preferences.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHandler {
    private static final Logger log = Logger.getLogger(FileHandler.class.getSimpleName());
    private static final Config config = Config.getInstance();

    // mod files
    private static Path getTempModDir(Game game) {
        return Path.of(game.getGameFolder(), "Mods", "SeriousContentManager");
    }
    private static Path getTempModDescriptor(Game game) {
        return Path.of(game.getGameFolder(), "Mods", "SeriousContentManager.des");
    }
    private static Path getTempModExlusionList(Game game) {
        return Path.of(game.getGameFolder(), "Mods", "SeriousContentManager", "BaseBrowseExclude.lst");
    }
    private static Path getTempModInclusionList(Game game) {
        return Path.of(game.getGameFolder(), "Mods", "SeriousContentManager", "BaseBrowseInclude.lst");
    }
    private static Path getTempModInclusionListWrite(Game game) {
        return Path.of(game.getGameFolder(), "Mods", "SeriousContentManager", "BaseWriteInclude.lst");
    }
    private static Path getTempModDataVar(Game game) {
        return Path.of(game.getGameFolder(), "Mods", "SeriousContentManager", "Data", "Var");
    }
    private static Path getTempModScripts(Game game) {
        return Path.of(game.getGameFolder(), "Mods", "SeriousContentManager", "Scripts");
    }
    private static Path getTempModGameStartupIni(Game game) {
        return Path.of(game.getGameFolder(), "Mods", "SeriousContentManager", "Scripts", "Game_startup.ini");
    }
    private static Path getTempModModNameVar(Game game) {
        return Path.of(game.getGameFolder(), "Mods", "SeriousContentManager", "Data", "Var", "ModName.var");
    }
    private static Path getTempModSamVersionVar(Game game) {
        return Path.of(game.getGameFolder(), "Mods", "SeriousContentManager", "Data", "Var", "Sam_Version.var");
    }

    public static void registerNewFile(ContentModel contentModel, String file) {
        try {
            // move to downloads
            Path tempFilePath = Path.of(file);
            Path downloads = Path.of(config.getDirectoryDownloads());
            if (!Files.exists(downloads)) {
                Files.createDirectory(downloads);
            }

            Path downloadPath = Path.of(downloads.toString(), tempFilePath.getFileName().toString());
            ContentFile newFile = categorizeFile(downloadPath);

            if (newFile != null) {
                move(tempFilePath, newFile.toPath());
                contentModel.setDownloadedFile(newFile);
                contentModel.setDownloadedFileName(newFile.getName());
                contentModel.setDateAdded(LocalDateTime.now());
                contentModel.setupListener();
                ContentRepository.getInstance().save(contentModel);
            } else {
                log.warning("Could not register new file: Unrecognized format.");
            }
        } catch (IOException e) {
            log.severe(e.toString());
        }
    }

    public static ContentFile categorizeFile(Path filePath) {
        if (filePath.getFileName().toString().toLowerCase().endsWith(".gro")) {
            return new GroFile(filePath.toString());
        } else if (filePath.getFileName().toString().toLowerCase().endsWith(".zip")) {
            return new ZipFile(filePath.toString());
        } else {
            // not recognized, what do?
            return null;
        }
    }

    public static void installContent(ContentModel contentModel) {
        try {
            Path sourcePath = contentModel.getDownloadedFile().toPath();
            Path targetPath = Path.of(contentModel.getGame().getGameFolder(), contentModel.getDownloadedFileName());
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            contentModel.setInstalled(true);

            // register new deployment on DB
            ContentRepository.getInstance().update(contentModel);
        } catch (IOException e) {
            log.warning(e.toString());
        }
    }

    public static void installContent(ContentModel contentModel, Path targetLocation) {
        try {
            Path sourcePath = contentModel.getDownloadedFile().toPath();
            Files.copy(sourcePath, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            contentModel.setInstalled(true);
            contentModel.setInstallFileLocation(targetLocation.toFile());

            // register new deployment on DB
            ContentRepository.getInstance().update(contentModel);
        } catch (IOException e) {
            log.warning(e.toString());
        }
    }

    public static void createTempMod(ContentModel contentModel) {
        try {
            log.info("Creating temp mod directory: " + getTempModDir(contentModel.getGame()));
            removeTempModFolder(contentModel); // if leftover
            Path modDir = Files.createDirectory(getTempModDir(contentModel.getGame()));

            // Set mod description to map name
            String descriptionContent = contentModel.getName() + " (SCM)";
            Files.writeString(getTempModDescriptor(contentModel.getGame()), descriptionContent);

            // exclude base game levels to only display custom map
            String excludeList = "Levels";
            Files.writeString(getTempModExlusionList(contentModel.getGame()), excludeList);
            String include = "SaveGame\nControls";
            Files.writeString(getTempModInclusionList(contentModel.getGame()), include);
            Files.writeString(getTempModInclusionListWrite(contentModel.getGame()), include);

            Files.createDirectories(getTempModDataVar(contentModel.getGame()));
            Files.writeString(getTempModModNameVar(contentModel.getGame()), descriptionContent);
            if (contentModel.getVersion() != null) {
                Files.writeString(getTempModSamVersionVar(contentModel.getGame()), contentModel.getVersion());
            }

            // define new game map name if only one map
            Path levelName = contentModel.getDownloadedFile().findFirstLevel();
            if (levelName != null) {
                String relativePath = levelName.toString().substring(1);
                if (relativePath.startsWith("\\")) {
                    relativePath = relativePath.substring(1);
                }
                relativePath = relativePath.replace("\\", "\\\\"); // backslashes in string need to be escaped for the file as well
                String startingMapEntry = "sam_strFirstLevel = \"" + relativePath + "\";";
                Files.createDirectory(getTempModScripts(contentModel.getGame()));
                Files.writeString(getTempModGameStartupIni(contentModel.getGame()), startingMapEntry);
            }

            File target = new File(modDir + File.separator + contentModel.getDownloadedFileName());
            if (contentModel.getDownloadedFile() instanceof GroFile) {
                installContent(contentModel, target.toPath());
            } else if (contentModel.getDownloadedFile() instanceof ZipFile) {
                // extract into mod dir
                extractZip(contentModel.getDownloadedFile(), modDir.toFile());

                contentModel.setInstalled(true);
                contentModel.setInstallFileLocation(target);

                // register new deployment on DB
                ContentRepository.getInstance().update(contentModel);
            } else {
                // we can't do anything here really
            }
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
    }

    private static void extractZip(File zip, File target) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(target, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    /**
     * The newFile() method guards against writing files to the file system outside the target folder. This vulnerability is called Zip Slip.
     * @param destinationDir
     * @param zipEntry
     * @return
     * @throws IOException
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private static void removeTempModFolder(ContentModel contentModel) throws IOException {
        if (Files.exists(getTempModDir(contentModel.getGame()))) {
            try (Stream<Path> pathStream = Files.walk(getTempModDir(contentModel.getGame()))) {
                log.info("Removing temp mod files...");
                pathStream.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                Files.delete(getTempModDescriptor(contentModel.getGame()));
            }
        }
    }

    public static void removeTempMod(ContentModel contentModel) {
        try {
            removeTempModFolder(contentModel);
            log.info("... done.");
            contentModel.setInstallFileLocation(null);
            contentModel.setInstalled(false);
            contentModel.setDateLastPlayed(LocalDateTime.now());
            ContentRepository.getInstance().update(contentModel);
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
    }

    public static Path move(Path source, Path destination) {
        try {
            return Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warning(e.toString());
        }

        return null;
    }

    public static void removeContent(ContentModel contentModel) {
        try {
            Files.delete(Path.of(contentModel.getGame().getGameFolder() + "/" + contentModel.getDownloadedFileName()));
            contentModel.setInstalled(false);
            ContentRepository.getInstance().update(contentModel);
        } catch (IOException e) {
            log.warning(e.toString());
        }
    }

    public static void deleteContent(ContentModel contentModel) {
        try {
            if (contentModel.isInstalled()) {
                removeContent(contentModel);
            }
            ContentRepository.getInstance().delete(contentModel);
            Files.delete(Path.of(config.getDirectoryDownloads(), contentModel.getDownloadedFileName()));
        } catch (IOException e) {
            log.warning(e.toString());
        }
    }
}
