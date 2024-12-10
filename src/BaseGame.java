import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class BaseGame {
    private long window;
    private int WIDTH = 800;
    private int HEIGHT = 600;

    private float playerX = 0.0f;
    private float playerY = 0.0f;
    private float playerSpeed = 0.05f;

    private int playerTexture;
    private int backgroundTexture;

    public static void main(String[] args) {
        new BaseGame().run();
    }

    public void run() {
        init();
        loop();

        // Free resources
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Initialize GLFW
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "2D Game", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Create OpenGL capabilities for the current context
        GL.createCapabilities();

        // Set the framebuffer size callback to handle window resizing
        glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
            glViewport(0, 0, width, height); // Update the viewport
            this.WIDTH = width;             // Update the stored width
            this.HEIGHT = height;           // Update the stored height
        });

        glfwSwapInterval(1); // Enable V-Sync
        glfwShowWindow(window); // Show the window

        // Enable 2D textures
        glEnable(GL_TEXTURE_2D);

        // Load your textures
        playerTexture = loadTexture("res/character.gif");
        backgroundTexture = loadTexture("res/background.png");
    }



    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            renderBackground();
            renderPlayer();

            glfwSwapBuffers(window);
            glfwPollEvents();
            handleInput();
        }
    }

    private void renderBackground() {
        glBindTexture(GL_TEXTURE_2D, backgroundTexture);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(-1, -1);
        glTexCoord2f(1, 0); glVertex2f(1, -1);
        glTexCoord2f(1, 1); glVertex2f(1, 1);
        glTexCoord2f(0, 1); glVertex2f(-1, 1);
        glEnd();
    }

    private void renderPlayer() {
        glBindTexture(GL_TEXTURE_2D, playerTexture);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(playerX - 0.1f, playerY - 0.1f);
        glTexCoord2f(1, 0); glVertex2f(playerX + 0.1f, playerY - 0.1f);
        glTexCoord2f(1, 1); glVertex2f(playerX + 0.1f, playerY + 0.1f);
        glTexCoord2f(0, 1); glVertex2f(playerX - 0.1f, playerY + 0.1f);
        glEnd();
    }


    private void handleInput() {
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
            playerX -= playerSpeed;
        }
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            playerX += playerSpeed;
        }
        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            playerY += playerSpeed;
        }
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            playerY -= playerSpeed;
        }
    }

    private int loadTexture(String filePath) {
        int textureID;
        ByteBuffer imageBuffer;
        IntBuffer width, height, channels;

        // Load image
        try (MemoryStack stack = stackPush()) {
            width = stack.mallocInt(1);
            height = stack.mallocInt(1);
            channels = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);
            imageBuffer = stbi_load(filePath, width, height, channels, 4);

            if (imageBuffer == null) {
                throw new RuntimeException("Failed to load texture file: " + filePath + "\n" + stbi_failure_reason());
            }

            textureID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureID);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            stbi_image_free(imageBuffer);
        }

        return textureID;
    }
}
