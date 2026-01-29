package com.miproject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.wicket.markup.html.form.upload.FileUpload;

public class ImageManager {
    
    private static final String UPLOAD_FOLDER = "webapp/uploads/carrusel/";
    
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    ));
    
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp"
    ));
    
    private static final List<String> DEFAULT_IMAGE_NAMES = Arrays.asList(
        "default1", "default2", "default3"
    );
    
    static {
        File uploadDir = new File(UPLOAD_FOLDER);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            System.out.println("Directorio de imágenes creado: " + uploadDir.getAbsolutePath());
        }
    }
    

    public static boolean isAllowedImage(FileUpload upload) {
        if (upload == null) {
            return false;
        }
        
        String fileName = upload.getClientFileName().toLowerCase();
        String contentType = upload.getContentType();
        
        boolean validExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                validExtension = true;
                break;
            }
        }
        
        boolean validMimeType = ALLOWED_MIME_TYPES.contains(contentType);
        
        return validExtension && validMimeType;
    }

    public static List<String> getCarruselImages() {
        List<String> allImages = new ArrayList<>();
        
        for (String imgName : DEFAULT_IMAGE_NAMES) {
            String imagePath = findDefaultImage(imgName);
            if (imagePath != null) {
                allImages.add(imagePath);
            }
        }
        
        File uploadDir = new File(UPLOAD_FOLDER);
        if (uploadDir.exists() && uploadDir.isDirectory()) {
            File[] uploadedFiles = uploadDir.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
            });
            
            if (uploadedFiles != null) {
                for (File file : uploadedFiles) {
                    // IMPORTANTE: Usar ruta web para el navegador
                    allImages.add("uploads/carrusel/" + file.getName());
                }
            }
        }
        
        if (allImages.isEmpty()) {
            allImages.addAll(Arrays.asList(
                "https://via.placeholder.com/800x400/4CAF50/FFFFFF?text=Carrusel+1",
                "https://via.placeholder.com/800x400/2196F3/FFFFFF?text=Carrusel+2",
                "https://via.placeholder.com/800x400/FF9800/FFFFFF?text=Carrusel+3"
            ));
        }
        
        return allImages;
    }
    

    public static List<String> getCarruselImages(boolean shuffle) {
        List<String> allImages = getCarruselImages();
        
        if (shuffle) {
            java.util.Collections.shuffle(allImages);
        }
        
        return allImages;
    }
    

    public static List<String> getUserImagesOnly() {
        List<String> userImages = new ArrayList<>();
        
        File uploadDir = new File(UPLOAD_FOLDER);
        if (uploadDir.exists() && uploadDir.isDirectory()) {
            File[] uploadedFiles = uploadDir.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
            });
            
            if (uploadedFiles != null) {
                for (File file : uploadedFiles) {
                    userImages.add("uploads/carrusel/" + file.getName());
                }
            }
        }
        
        return userImages;
    }
    

    public static boolean hasUserImages() {
        return !getUserImagesOnly().isEmpty();
    }
    

    public static int getUserImageCount() {
        return getUserImagesOnly().size();
    }
    

    private static String findDefaultImage(String baseName) {
    String[] extensions = {".jpg", ".jpeg", ".png", ".gif"};
    
    File imgFile;
    
    for (String ext : extensions) {
        imgFile = new File("webapp/images/" + baseName + ext);
        if (imgFile.exists()) {
            return "images/" + baseName + ext;
        }
    }
    
    for (String ext : extensions) {
        imgFile = new File("src/main/webapp/images/" + baseName + ext);
        if (imgFile.exists()) {
            return "images/" + baseName + ext;
        }
    }
    
    return null;
}
    

    public static boolean saveUploadedImage(FileUpload upload) {
        if (upload == null) {
            return false;
        }
        
        if (!isAllowedImage(upload)) {
            System.err.println("Tipo de archivo no permitido: " + upload.getContentType());
            return false;
        }
        
        try {
            String originalName = upload.getClientFileName();
            String extension = getFileExtension(originalName);
            
            if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                extension = ".jpg";
            }
            
            String fileName = "user_" + System.currentTimeMillis() + extension;
            File outputFile = new File(UPLOAD_FOLDER + fileName);
            
            // Guardar el archivo
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(upload.getBytes());
            }
            
            System.out.println("Imagen guardada exitosamente: " + outputFile.getName());
            System.out.println("Ruta web: uploads/carrusel/" + fileName);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error al guardar imagen: " + e.getMessage());
            return false;
        }
    }
    

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex).toLowerCase();
        }
        return "";
    }
    

    public static boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        
        if (imagePath.startsWith("images/") || imagePath.contains("placeholder.com")) {
            return false;
        }
        
        File imageFile;
        if (imagePath.startsWith(UPLOAD_FOLDER)) {
            imageFile = new File(imagePath);
        } else if (imagePath.startsWith("uploads/carrusel/")) {
            imageFile = new File(UPLOAD_FOLDER + imagePath.substring("uploads/carrusel/".length()));
        } else {
            imageFile = new File(UPLOAD_FOLDER + imagePath);
        }
        
        if (imageFile.exists() && imageFile.isFile()) {
            return imageFile.delete();
        }
        
        return false;
    }
    
    public static int getImageCount() {
        return getCarruselImages().size();
    }
    
    public static void testImages() {
        System.out.println("=== PRUEBA DE IMAGENES DEL CARRUSEL ===");
        
        List<String> imagesNormal = getCarruselImages();
        List<String> imagesShuffled = getCarruselImages(true);
        
        System.out.println("Total de imágenes encontradas: " + imagesNormal.size());
        System.out.println("Imágenes de usuario: " + getUserImageCount());
        
        System.out.println("\nImágenes (orden normal):");
        for (int i = 0; i < imagesNormal.size(); i++) {
            System.out.println("Imagen " + (i+1) + ": " + imagesNormal.get(i));
        }
        
        System.out.println("\nImágenes (mezcladas):");
        for (int i = 0; i < imagesShuffled.size(); i++) {
            System.out.println("Imagen " + (i+1) + ": " + imagesShuffled.get(i));
        }
        
        System.out.println("=== FIN DE PRUEBA ===");
    }
}