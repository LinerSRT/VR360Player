package ru.liner.vr360server.utils;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PickerFileFilter implements FileFilter {
    private final boolean allowDirectories;
    private final FileType fileType;
    private List<File> foundedFiles;

    public PickerFileFilter(boolean allowDirectories, FileType fileType) {
        this.allowDirectories = allowDirectories;
        this.fileType = fileType;
        this.foundedFiles = new ArrayList<>();
    }


    public PickerFileFilter(FileType fileType) {
        this(true, fileType);
    }

    @Override
    public boolean accept(File file) {
        if (file.isHidden() || !file.canRead()) {
            return false;
        }
        if (file.isDirectory()) {
            return checkDirectory(file);
        }
        return checkFileExtension(file);
    }

    private boolean checkFileExtension(File file) {
        switch (fileType) {
            case DOCUMENT:
                return FileUtils.TextFormat.SUPPORTED_LIST.contains(FileUtils.getMimeType(file.getAbsolutePath()));
            case ARCHIVE:
                return FileUtils.FileFormat.SUPPORTED_LIST.contains(FileUtils.getMimeType(file.getAbsolutePath()));
            case VIDEO:
                return FileUtils.VideoFormat.SUPPORTED_LIST.contains(FileUtils.getMimeType(file.getAbsolutePath()));
            case IMAGE:
                return FileUtils.ImageFormat.SUPPORTED_LIST.contains(FileUtils.getMimeType(file.getAbsolutePath()));
            case ALL:
            default:
                return true;
        }
    }


    private boolean checkDirectory(File dir) {
        if (!allowDirectories) {
            return false;
        } else {
            final ArrayList<File> subDirs = new ArrayList<>();
            int songNumb = Objects.requireNonNull(dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isFile()) {
                        return checkFileExtension(file);
                    } else if (file.isDirectory()) {
                        subDirs.add(file);
                        return false;
                    } else
                        return false;
                }
            })).length;

            if ( songNumb > 0 ) {
                return true;
            }

            for( File subDir: subDirs ) {
                if ( checkDirectory( subDir ) ) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum FileType {
        ALL,
        IMAGE,
        ARCHIVE,
        VIDEO,
        DOCUMENT
    }


}