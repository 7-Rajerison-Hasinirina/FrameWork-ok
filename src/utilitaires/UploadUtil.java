package utilitaires;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Properties;

import jakarta.servlet.http.Part;

public class UploadUtil {

    private static final String[] PROPERTY_KEYS = {
            "upload.directory",
            "upload.folder",
            "upload.path",
            "framework.upload.directory",
            "framework.upload.folder"
    };

    public static String getUploadDirectory() throws IOException {
        String configured = System.getProperty("upload.directory");
        if (configured != null && !configured.trim().isEmpty()) {
            return configured.trim();
        }

        Properties props = new Properties();
        loadProperties(props, "/framework.properties");
        loadProperties(props, "framework.properties");

        for (String key : PROPERTY_KEYS) {
            String value = props.getProperty(key);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return Paths.get(System.getProperty("user.dir"), "uploads").toString();
    }

    public static File saveUploadedFile(Part part) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }

        String dirPath = getUploadDirectory();
        File directory = new File(dirPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Impossible de créer le dossier d'upload : " + dirPath);
        }

        String submittedFileName = part.getSubmittedFileName();
        String fileName = (submittedFileName == null || submittedFileName.trim().isEmpty())
                ? "upload-" + System.currentTimeMillis()
                : new File(submittedFileName).getName();

        File targetFile = new File(directory, fileName);

        try (InputStream in = part.getInputStream(); OutputStream out = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        return targetFile;
    }

    private static void loadProperties(Properties properties, String resourcePath) throws IOException {
        InputStream inputStream = null;
        try {
            if (resourcePath.startsWith("/")) {
                inputStream = UploadUtil.class.getResourceAsStream(resourcePath);
            } else {
                File file = new File(resourcePath);
                if (file.exists()) {
                    inputStream = new FileInputStream(file);
                } else {
                    inputStream = UploadUtil.class.getResourceAsStream("/" + resourcePath);
                }
            }

            if (inputStream != null) {
                properties.load(inputStream);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
