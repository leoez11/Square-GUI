package pkgDriver;


import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Driver {
    GLFWErrorCallback errorCallback;
    GLFWKeyCallback keyCallback;
    GLFWFramebufferSizeCallback fbCallback;
    long window;
    static int WIN_WIDTH = 800, WIN_HEIGHT = 800;
    int WIN_POS_X = 30, WIN_POX_Y = 90;
    private static final int OGL_MATRIX_SIZE = 16;
    // call glCreateProgram() here - we have no gl-context here
    int shader_program;
    Matrix4f viewProjMatrix = new Matrix4f();

    private static final float SQUARE_SIZE = 20f;
    private static final float VIEW_LEFT = -100;
    private static final float VIEW_RIGHT = 100;
    private static final float VIEW_BOTTOM = -100;
    private static final float VIEW_TOP = 100;
    private static final float VIEW_NEAR = 0;
    private static final float VIEW_FAR = 10;
    private static final float SQUARE_TRANSLATE_X = 20f;
    private static final float SQUARE_TRANSLATE_Y = 20f;

    private static final float BACKGROUND_COLOR_R = 0.0f;
    private static final float BACKGROUND_COLOR_G = 0.0f;
    private static final float BACKGROUND_COLOR_B = 1.0f;
    private static final float BACKGROUND_COLOR_A = 1.0f;

    private static final float SQUARE_COLOR_R = 1.0f;
    private static final float SQUARE_COLOR_G = 0.5f;
    private static final float SQUARE_COLOR_B = 0.7f;
    private static final float SQUARE_COLOR_A = 1.0f;

    FloatBuffer myFloatBuffer = BufferUtils.createFloatBuffer(OGL_MATRIX_SIZE);
    int vpMatLocation = 0, renderColorLocation = 0;

    public static void main(String[] myArgs) {
        new Driver().render();
    } 
    void render() {
        try {
            initGLFWindow();
            renderLoop();
            glfwDestroyWindow(window);
            keyCallback.free();
            fbCallback.free();
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    } // void render()
    private void initGLFWindow() {
        glfwSetErrorCallback(errorCallback =
                GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 8);
        window = glfwCreateWindow(WIN_WIDTH, WIN_HEIGHT, "CSC 133", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int
                    mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
            }
        });
        glfwSetFramebufferSizeCallback(window, fbCallback = new
                GLFWFramebufferSizeCallback() {
                    @Override
                    public void invoke(long window, int w, int h) {
                        if (w > 0 && h > 0) {
                            WIN_WIDTH = w;
                            WIN_HEIGHT = h;
                        }
                    }
                });
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, WIN_POS_X, WIN_POX_Y);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    } // private void initGLFWindow()
    void renderLoop() {
        initOpenGL();
        initBuffers();
        drawScene();
    }
     // void renderLoop()
    void initOpenGL() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glViewport(0, 0, WIN_WIDTH, WIN_HEIGHT);
        glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        this.shader_program = glCreateProgram();
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
                "uniform mat4 viewProjMatrix;" +
                        "void main(void) {" +
                        " gl_Position = viewProjMatrix * gl_Vertex;" +
                        "}");
        glCompileShader(vs);
        glAttachShader(shader_program, vs);
        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
                "uniform vec3 color;" +
                        "void main(void) {" +
                        " gl_FragColor = vec4(1.0f, 0.5f, 0.7f, 1.0f);"  +
                        "}");
        glCompileShader(fs);
        glAttachShader(shader_program, fs);
        glLinkProgram(shader_program);
        glUseProgram(shader_program);
        vpMatLocation = glGetUniformLocation(shader_program, "viewProjMatrix");
    }

    void initBuffers() {
        int vbo = glGenBuffers();
        int ibo = glGenBuffers();
        float[] vertices = {
                -SQUARE_SIZE, -SQUARE_SIZE,
                SQUARE_SIZE, -SQUARE_SIZE,
                SQUARE_SIZE,  SQUARE_SIZE,
                -SQUARE_SIZE,  SQUARE_SIZE
        };
        int[] indices = {0, 1, 2, 0, 2, 3};
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (FloatBuffer) BufferUtils.
                createFloatBuffer(vertices.length).
                put(vertices).flip(), GL_STATIC_DRAW);
        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer) BufferUtils.
                createIntBuffer(indices.length).
                put(indices).flip(), GL_STATIC_DRAW);
        glVertexPointer(2, GL_FLOAT, 0, 0L);
    }

    void drawScene() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            drawSquares();

            glfwSwapBuffers(window);
        }
    }

    void drawSquares() {
        drawSquare(-SQUARE_TRANSLATE_X, SQUARE_TRANSLATE_Y);
        drawSquare(SQUARE_TRANSLATE_X, -SQUARE_TRANSLATE_Y);
    }

    void drawSquare(float x, float y) {
        viewProjMatrix.setOrtho(VIEW_LEFT, VIEW_RIGHT, VIEW_BOTTOM, VIEW_TOP, VIEW_NEAR, VIEW_FAR).translate(x, y, 0);
        glUniformMatrix4fv(vpMatLocation, false, viewProjMatrix.get(myFloatBuffer));
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0L);
    }
}


