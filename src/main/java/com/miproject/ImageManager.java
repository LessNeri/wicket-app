package com.miproject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import org.apache.wicket.markup.html.form.upload.FileUpload;

public class ImageManager {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".webp"
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static File getUploadDir() {
        String path = System.getenv("UPLOAD_DIR");
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("UPLOAD_DIR no está configurada");
        }
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }


    public static boolean isAllowedImage(FileUpload upload) {
        if (upload == null) return false;

        String name = upload.getClientFileName().toLowerCase();
        String type = upload.getContentType();

        return ALLOWED_MIME_TYPES.contains(type)
            && ALLOWED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private static int getNextImageNumber() {
        File dir = getUploadDir();
        int max = 0;

        File[] files = dir.listFiles((d, n) ->
            n.matches("img_\\d+\\.(jpg|jpeg|png|webp)")
        );

        if (files != null) {
            for (File f : files) {
                String num = f.getName().replaceAll("\\D+", "");
                max = Math.max(max, Integer.parseInt(num));
            }
        }
        return max + 1;
    }

    public static boolean saveUploadedImage(FileUpload upload) {
        if (!isAllowedImage(upload)) return false;

        try {
            int next = getNextImageNumber();
            File out = new File(getUploadDir(), "img_" + next + ".jpeg");

            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(upload.getBytes());
            }

            System.out.println("Imagen guardada: " + out.getAbsolutePath());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getCarruselImages(boolean shuffle) {
    List<String> images = new ArrayList<>();

    images.addAll(getDefaultImages());

    File dir = getUploadDir();
    File[] files = dir.listFiles((d, n) ->
        n.matches("img_\\d+\\.(jpg|jpeg|png|webp)")
    );

    if (files != null) {
        Arrays.sort(files, Comparator.comparingInt(
            f -> Integer.parseInt(f.getName().replaceAll("\\D+", ""))
        ));

        for (File f : files) {
            images.add("uploads/carrusel/" + f.getName());
        }
    }

    if (shuffle) Collections.shuffle(images);
    return images;
}



    public static List<String> getCarruselImages() {
        return getCarruselImages(false);
    }

    private static List<String> getDefaultImages() {
    return List.of(
        "images/default1.jpg",
        "images/default2.jpeg",
        "images/default3.jpeg"
    );
}

}