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
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHandler {
    private static final Logger log = Logger.getLogger(FileHandler.class.getSimpleName());
    private static final Config config = Config.getInstance();

    // mod files
    private static File getTempModDir(Game game) {
        return new File(game.getGameFolder() + "/Mods/SeriousContentManager");
    }
    private static File getTempModDescriptor(Game game) {
        return new File(game.getGameFolder() + "/Mods/SeriousContentManager.des");
    }
    private static File getTempModExlusionList(Game game) {
        return new File(game.getGameFolder() + "/Mods/SeriousContentManager/BaseBrowseExclude.lst");
    }
    private static File getTempModInclusionList(Game game) {
        return new File(game.getGameFolder() + "/Mods/SeriousContentManager/BaseBrowseInclude.lst");
    }
    private static File getTempModInclusionListWrite(Game game) {
        return new File(game.getGameFolder() + "/Mods/SeriousContentManager/BaseWriteInclude.lst");
    }
    private static File getTempModDataVar(Game game) {
        return new File(game.getGameFolder() + "/Mods/SeriousContentManager/Data/Var");
    }
    private static File getTempModScripts(Game game) {
        return new File(game.getGameFolder() + "/Mods/SeriousContentManager/Scripts");
    }
    private static File getTempModGameStartupIni(Game game) {
        return new File(game.getGameFolder() + "/Mods/SeriousContentManager/Scripts/Game_startup.ini");
    }
    private static File getTempModModNameVar(Game game) {
        return new File(game.getGameFolder() + "/Mods/SeriousContentManager/Data/Var/ModName.var");
    }
    private static File getTempModSamVersionVar(Game game) {
        return new File(game.getGameFolder() + "/Mods/SeriousContentManager/Data/Var/Sam_Version.var");
    }

    public static void registerNewFile(String file, String originURL, String name) {
        try {
            // move to downloads
            Path filePath = Path.of(file);
            Path downloads = Path.of(config.getDirectoryDownloads());
            if (!Files.exists(downloads)) {
                Files.createDirectory(downloads);
            }

            Path newFileLocation = Files.move(filePath, Path.of(downloads.toString() + "/" + filePath.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
            ContentEntity newFile = new ContentEntity();
            newFile.setName(name);
            newFile.setOrigin(originURL);
            newFile.setDownloadedFileName(newFileLocation.getFileName().toString());
            DBManager.getInstance().registerNewFile(newFile);
        } catch (IOException e) {
            log.severe(e.toString());
        }     
        
    }

    public static void registerNewFile(ContentEntity contentEntity, String file) {
        try {
            // move to downloads
            Path tempFilePath = Path.of(file);
            Path downloads = Path.of(config.getDirectoryDownloads());
            if (!Files.exists(downloads)) {
                Files.createDirectory(downloads);
            }

            String downloadPathName = downloads + "/" + tempFilePath.getFileName();
            ContentFile newFile = categorizeFile(downloadPathName);

            if (newFile != null) {
                move(tempFilePath, newFile.toPath());
                contentEntity.setDownloadedFile(newFile);
                contentEntity.setDownloadedFileName(newFile.getName());
                DBManager.getInstance().registerNewFile(contentEntity);
            } else {
                log.warning("Could not register new file: Unrecognized format.");
            }
        } catch (IOException e) {
            log.severe(e.toString());
        }
    }

    public static ContentFile categorizeFile(String filePath) {
        if (filePath.toLowerCase().endsWith(".gro")) {
            return new GroFile(filePath);
        } else if (filePath.toLowerCase().endsWith(".zip")) {
            return new ZipFile(filePath);
        } else {
            // not recognized, what do?
            return null;
        }
    }

    public static void installContent(ContentEntity contentEntity) {
        try {
            Path sourcePath = contentEntity.getDownloadedFile().toPath();
            Path targetPath = Path.of(contentEntity.getGame().getGameFolder() + "/" + contentEntity.getDownloadedFileName());
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            contentEntity.setInstalled(true);

            // register new deployment on DB
            DBManager.getInstance().update(contentEntity);
        } catch (IOException e) {
            log.warning(e.toString());
        }
    }

    public static void installContent(ContentEntity contentEntity, Path targetLocation) {
        try {
            Path sourcePath = contentEntity.getDownloadedFile().toPath();
            Files.copy(sourcePath, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            contentEntity.setInstalled(true);
            contentEntity.setInstallFileLocation(targetLocation.toFile());

            // register new deployment on DB
            DBManager.getInstance().update(contentEntity);
        } catch (IOException e) {
            log.warning(e.toString());
        }
    }

    public static void createTempMod(ContentEntity contentEntity) {
        try {
            log.info("Creating temp mod directory: " + getTempModDir(contentEntity.getGame()).toPath());
            removeTempModFolder(contentEntity); // if leftover
            Path modDir = Files.createDirectory(getTempModDir(contentEntity.getGame()).toPath());

            // Set mod description to map name
            String descriptionContent = contentEntity.getName() + " (SCM)";
            Files.writeString(getTempModDescriptor(contentEntity.getGame()).toPath(), descriptionContent);

            // exclude base game levels to only display custom map
            String excludeList = "Levels";
            Files.writeString(getTempModExlusionList(contentEntity.getGame()).toPath(), excludeList);
            String include = "SaveGame\nControls";
            Files.writeString(getTempModInclusionList(contentEntity.getGame()).toPath(), include);
            Files.writeString(getTempModInclusionListWrite(contentEntity.getGame()).toPath(), include);

            Files.createDirectories(getTempModDataVar(contentEntity.getGame()).toPath());
            Files.writeString(getTempModModNameVar(contentEntity.getGame()).toPath(), descriptionContent);
            if (contentEntity.getVersion() != null) {
                Files.writeString(getTempModSamVersionVar(contentEntity.getGame()).toPath(), contentEntity.getVersion());
            }

            // define new game map name if only one map
            Path levelName = contentEntity.getDownloadedFile().findFirstLevel();
            if (levelName != null) {
                String relativePath = levelName.toString().substring(1);
                if (relativePath.startsWith("\\")) {
                    relativePath = relativePath.substring(1);
                }
                relativePath = relativePath.replace("\\", "\\\\"); // backslashes in string need to be escaped for the file as well
                String startingMapEntry = "sam_strFirstLevel = \"" + relativePath + "\";";
                Files.createDirectory(getTempModScripts(contentEntity.getGame()).toPath());
                Files.writeString(getTempModGameStartupIni(contentEntity.getGame()).toPath(), startingMapEntry);
            }

            File target = new File(modDir + File.separator + contentEntity.getDownloadedFileName());
            if (contentEntity.getDownloadedFile() instanceof GroFile) {
                installContent(contentEntity, target.toPath());
            } else if (contentEntity.getDownloadedFile() instanceof ZipFile) {
                // extract into mod dir
                extractZip(contentEntity.getDownloadedFile(), modDir.toFile());

                contentEntity.setInstalled(true);
                contentEntity.setInstallFileLocation(target);

                // register new deployment on DB
                DBManager.getInstance().update(contentEntity);
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

    private static void removeTempModFolder(ContentEntity contentEntity) throws IOException {
        if (getTempModDir(contentEntity.getGame()).exists()) {
            try (Stream<Path> pathStream = Files.walk(getTempModDir(contentEntity.getGame()).toPath())) {
                log.info("Removing temp mod files...");
                pathStream.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                Files.delete(getTempModDescriptor(contentEntity.getGame()).toPath());
            }
        }
    }

    public static void removeTempMod(ContentEntity contentEntity) {
        try {
            removeTempModFolder(contentEntity);
            log.info("... done.");
            contentEntity.setInstallFileLocation(null);
            contentEntity.setInstalled(false);
            DBManager.getInstance().update(contentEntity);
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

    public static void removeContent(ContentEntity contentEntity) {
        try {
            Files.delete(Path.of(contentEntity.getGame().getGameFolder() + "/" + contentEntity.getDownloadedFileName()));
            contentEntity.setInstalled(false);
            DBManager.getInstance().update(contentEntity);
        } catch (IOException e) {
            log.warning(e.toString());
        }
    }

    public static void deleteContent(ContentEntity contentEntity) {
        try {
            if (contentEntity.isInstalled()) {
                removeContent(contentEntity);
            }
            DBManager.getInstance().delete(contentEntity);
            Files.delete(Path.of(config.getDirectoryDownloads() + "/" + contentEntity.getDownloadedFileName()));
        } catch (IOException e) {
            log.warning(e.toString());
        }
    }
}
