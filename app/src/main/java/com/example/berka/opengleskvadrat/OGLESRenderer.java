package com.example.berka.opengleskvadrat;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by berka on 9/1/2016.
 */
public class OGLESRenderer implements GLSurfaceView.Renderer {


       Context con;
    private float[] modelMatrix = new float[16]; //inicijalizacija model matrice

    private float[] mViewMatrix = new float[16]; //inicijalizacija view matrice

    private float[] projectionMatrix = new float[16];//inicijalizacija projection matrice

    private float[] mVPMatrix = new float[16]; //inicijalizacija model view matrice



    // definisanje bafera u koje cemo smestiti informacije o verteksima
    private final FloatBuffer floatBuffer;
    private final FloatBuffer colorBuffer;
    ShortBuffer indexBuffer = null;



    /** This will be used to pass in the transformation matrix. */
    private int mvpMatrixHandle;

    /** This will be used to pass in model position information. */
    private int positionHandle;

    /** This will be used to pass in model color information. */
    private int colorHandle;

    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;




    // definisanje indeksa koji ce se koristiti za iscrtavanje kvadrata
    short[] indeces={
            0,1,2,
            0,3,2

    };


    public OGLESRenderer(Context con) {
        this.con=con;

        // ovom promenljivom definisemo koordinate kocke u lokalnom koordinatnom sistemu
        final float[] square={
                0.0f, 1.0f, 0.0f,
                0.0f,0.0f,0.0f,
                1.0f,0.0f,0.0f,
                1.0f,1.0f,0.0f

        };



        // definisanje boje kvadrata
        final float[] colors = {1,0,0,
                1,0,0,
                1,0,0,
                1,0,0
        };


        // inicijalizujemo bafer, koji ce da cuva podatke o koordinatama kvadrata
        floatBuffer = ByteBuffer.allocateDirect(square.length *4). order(ByteOrder.nativeOrder()).asFloatBuffer();
        floatBuffer.put(square).position(0);


// definisanje bafera u koje cemo smestiti indekse
        indexBuffer = ByteBuffer.allocateDirect(indeces.length * 2) .order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indeces).position(0);


        // definisanje bafera u koje cemo smestiti informacije o boji kvadrata
        colorBuffer = ByteBuffer.allocateDirect(colors.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors).position(0);


    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        // podešavamo boju pozadine scene, da bude siva
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // podešavamo view matricu. Ova matrica će predstavljati poziciju kamere
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -5, 0, 0, 0, 0, 1, 0);


        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);


        // definisanje shader-a, koji će da računa poziciju svakog verteksa
        final String vertexShader =
                "uniform mat4 un_MVPMatrix;      \n"
                        + "attribute vec4 attribute_Position;     \n"
                        + "attribute vec4 attribute_Color;        \n"

                        + "varying vec4 var_Color;            \n"

                        + "void main()                        \n"
                        + "{                                  \n"
                        + "   var_Color = attribute_Color;          \n"

                        + "   gl_Position = un_MVPMatrix      \n"
                        + "               * attribute_Position;   \n"
                        + "}                                 \n";


        // definisanje shader-a, koji će da dodeljuje određenu boju svakom pikselu na ekranu
        final String fragmentShader =
                "precision mediump float;       \n"

                        + "varying vec4 var_Color;          \n"

                        + "void main()                    \n"
                        + "{                              \n"
                        + "   gl_FragColor = var_Color;     \n"
                        + "}                              \n";


        // učitavamo vreteks shader
        int vertexS = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexS != 0)
        {
            // prosleđujemo izvorni kod
            GLES20.glShaderSource(vertexS, vertexShader);

            // kompajliramo shader
            GLES20.glCompileShader(vertexS);

            // dobijamo status kompajliranja
            final int[] compile_Status = new int[1];
            GLES20.glGetShaderiv(vertexS, GLES20.GL_COMPILE_STATUS, compile_Status, 0);


        }

        // ako je objekat verteks shader-a prazan, pozivamo izuzetak
        if (vertexS == 0)
        {
            try {
                throw new Exception("Vertex shader is not created.");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // učitavamo fragment shader
        int fragmentS = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        if (fragmentS != 0)
        {
            // prosleđujemo objektu fragment shader-a izvorni kod
            GLES20.glShaderSource(fragmentS, fragmentShader);

            // vršimo kompilaciju shader-a
            GLES20.glCompileShader(fragmentS);

            // dobijamo status kompajliranja
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentS, GLES20.GL_COMPILE_STATUS, compileStatus, 0);


        }

        // ukoliko je objekat fragment shader-a prazan, pozivamo izuzetak
        if (fragmentS == 0)
        {
            try {
                throw new Exception("Fragment shader is not created.");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // kreiramo objekat programa
        int program = GLES20.glCreateProgram();

        if (program != 0)
        {
            // prosleđujemo programu verteks shader
            GLES20.glAttachShader(program, vertexS);

            // prosleđujemo programu fragment shader
            GLES20.glAttachShader(program, fragmentS);

            // dodeljujemo programu atribute iz verteks shader-a
            GLES20.glBindAttribLocation(program, 0, "attribute_Position");
            GLES20.glBindAttribLocation(program, 1, "attribute_Color");

            // povezujemo dva shader-a u program
            GLES20.glLinkProgram(program);

            // dobijamo status povezivanja shader-a u program
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);

        }
        // ako je program prazan, pozivamo izuzetak
        if (program == 0)
        {
            try {
                throw new Exception("Program error");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // preuzimamo iz shader-a vrednosti matrice transformacije i atributa
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "un_MVPMatrix");
        positionHandle = GLES20.glGetAttribLocation(program, "attribute_Position");
        colorHandle = GLES20.glGetAttribLocation(program, "attribute_Color");

        // obaveštavamo openGL da želimo da upotrebi ovaj program prilikom renderovanja
        GLES20.glUseProgram(program);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {

        // podešavamo viewport aplikacije
        GLES20.glViewport(0, 0, width, height);


        // podešavamo parametre za projekcionu matricu
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        // kreiramo novu matricu projekcije, koja će da realizuje perspektivnu
        // projekciju. Visina će ostati ista
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // postavljamo model matricu da bude jedinicna matrica
        Matrix.setIdentityM(modelMatrix, 0);



        // vrsimo translaciju kvadrata po X osi
        Matrix.translateM(modelMatrix,0,  -0.5f, 0.0f, 0.0f);

        //tj ubacuje se to iznad

// pozivamo metodu kojom se definise objekat na ekranu, prosledjujemo verteks i
// indeks bafer sa podacima
        drawObject(floatBuffer,indexBuffer);

        //ODAVDE IDE NOVO

        // onemogucen cull facing
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // onemoguceno depth testiranje
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);


// omogucen blending
        GLES20.glEnable(GLES20.GL_BLEND);

// podesavanje efekta blendinga
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

// podesavamo jedinicnu matricu
        Matrix.setIdentityM(modelMatrix, 0);

// vrsimo translaciju kvadrata po X, Y i Z osi
        Matrix.translateM(modelMatrix, 0, 0.3f, 0.4f, -0.5f);

// iscrtavamo kvadrat
        drawObject (floatBuffer, indexBuffer);

// deaktiviramo funkciju blendinga
        GLES20.glDisable(GLES20.GL_BLEND);

    }

    // kreiramo metodu koja ce da na osnovu podataka koje smo definisali da crta objekat
    private void drawObject(final FloatBuffer fb,ShortBuffer sb)
    {

        // podesavamo poziciju vertexBuffer-a
        fb.position(0);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, fb);
        GLES20.glEnableVertexAttribArray(positionHandle);

        // podesavamo poziciju colorBuffer-a
        colorBuffer.position(0);
        GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_FLOAT, false,
                0, colorBuffer);

        GLES20.glEnableVertexAttribArray(colorHandle);


// mnozimo view matricu sa model matricom i rezultat cuvamo u mVP matrici (model view // matrica)
        Matrix.multiplyMM(mVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);

//Mnozimo model view matricu sa projekcionom matricom i cuvamo rezultata mnozenja u //mVP matrici
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, mVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mVPMatrix, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indeces.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    }

}
