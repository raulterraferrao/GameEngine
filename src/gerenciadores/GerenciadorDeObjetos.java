package gerenciadores;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import objetos.Objeto;
import texturas.Textura;

public class GerenciadorDeObjetos {

	private static final int POSICAO = 0;
	private static final int TEXTURA = 1;
	private static final int NORMAL = 2;

	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> texturas = new ArrayList<Integer>();

	public Objeto carregarParaVAO(float[] vertices, float[] coordenadasTextura, float[] normais, int[] indices) {

		int vaoID = criarVAO();
		carregarBufferDeIndices(indices);
		carregarVBOParaVAO(POSICAO, 3, vertices);
		carregarVBOParaVAO(TEXTURA, 2, coordenadasTextura);
		carregarVBOParaVAO(NORMAL, 3, normais);
		unbindVAO();

		return new Objeto(vaoID, indices.length);

	}
	
	public int carregarParaVAO(float[] vertices, float[] coordenadasTextura) {

		int vaoID = criarVAO();
		carregarVBOParaVAO(POSICAO, 2, vertices);
		carregarVBOParaVAO(TEXTURA, 2, coordenadasTextura);
		unbindVAO();

		return vaoID;

	}

	public Objeto carregarParaVAO(float[] vertices, int dimensoes) {
		int vaoID = criarVAO();
		carregarVBOParaVAO(POSICAO, dimensoes, vertices);
		unbindVAO();

		return new Objeto(vaoID, vertices.length / dimensoes);
	}

	private int criarVAO() {
		// Geramos um VAO e recebemos o seu ID
		int vaoID = GL30.glGenVertexArrays();
		// Ao criar um vao colocamos-o na lista para depois apaga-los
		vaos.add(vaoID);
		// Para podermos utilizar o nosso VAO necessitamos ativá-lo
		GL30.glBindVertexArray(vaoID);

		return vaoID;
	}

	private void carregarVBOParaVAO(int pos, int tamanhoCoordenadas, float[] dados) {
		// Geramos um VBO e recebemos o seu ID
		int vboID = GL15.glGenBuffers();
		// Ao criar um vbo colocamos-o na lista para depois apaga-los
		vbos.add(vboID);
		// O nosso VBO vai ser um Array_Buffer e passamos tbm o ID do nosso VBO
		// para dar Bind
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		// Tranformamos os dados de array de float para um buffer de float
		FloatBuffer buffer = guardarDadosComoFloatBuffer(dados);
		// Aqui nós guardamos esse buffer no VBO
		// Utilizamos o (usage) como static pois não vamos modifica-lo após
		// cria-lo
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		// Agora colocamos o nosso VBO no nosso VAO
		GL20.glVertexAttribPointer(pos, tamanhoCoordenadas, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	/***
	 * Carrega as texturas que estão na past img/ , lembrando que devem todas
	 * ser extensão png.
	 * 
	 * @param nomeArquivo
	 * @return texturaID
	 */
	public int carregarTextura(String nomeArquivo) {

		Texture textura = null;

		try {
			textura = TextureLoader.getTexture("PNG", new FileInputStream("img/" + nomeArquivo + ".png"));
			// MIPMAP GANHO DE PERFORMACE GIGANTE
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.4f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int texturaID = textura.getTextureID();
		texturas.add(texturaID);
		return texturaID;

	}

	public int carregarCubeMap(String[] nomeArquivo) {
		int texturaID = GL11.glGenTextures();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texturaID);

		for (int i = 0; i < nomeArquivo.length; i++) {
			Textura textura = carregarTexturaPNG("img/" + nomeArquivo[i] + ".png");
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, textura.getLargura(),
					textura.getAltura(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textura.getBuffer());
		}
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE); 
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		texturas.add(texturaID);
		return texturaID;
	}

	public Textura carregarTexturaPNG(String nomeArquivo) {

		int largura = 0;
		int altura = 0;
		ByteBuffer buffer = null;

		try {
			FileInputStream in = new FileInputStream(nomeArquivo);
			PNGDecoder decoder = new PNGDecoder(in);
			largura = decoder.getWidth();
			altura = decoder.getHeight();
			buffer = ByteBuffer.allocateDirect(4 * largura * altura);
			decoder.decode(buffer, largura * 4, Format.RGBA);
			buffer.flip();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("CarregarTexturaPNG " + nomeArquivo + ", não funcionou");
			System.exit(-1);
		}
		return new Textura(buffer, largura, altura);

	}

	/***
	 * VBO de indices no quais os vertices est�o conectados, esse m�todo diz a
	 * gpu qual vertice est� conectado com qual.
	 * 
	 * @param indices
	 */
	private void carregarBufferDeIndices(int[] indices) {
		// Exatamente como um buffer normal por�m o Target � um
		// Element_array_buffer

		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = guardarDadosComoIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

	}

	private void unbindVAO() {
		// Aqui demos Unbind no VAO pois paramos de usá-lo
		// Utilizamos o 0 para dar unbind
		GL30.glBindVertexArray(0);

	}

	// Método usado para quando querermos fechar a Engine nós tiramos todos os
	// VAOs e VBOs da memória
	public void desalocar() {
		for (int vao : vaos) {
			GL30.glDeleteVertexArrays(vao);
		}
		for (int vbo : vbos) {
			GL15.glDeleteBuffers(vbo);
		}
		for (int textura : texturas) {
			GL11.glDeleteTextures(textura);
		}
	}

	private IntBuffer guardarDadosComoIntBuffer(int[] dados) {
		// Criamos um buffer de int para podermos conseguir ler os dados do VBO
		IntBuffer buffer = BufferUtils.createIntBuffer(dados.length);
		// Colocamos os dados passado como parâmetro no buffer
		buffer.put(dados);
		// Utilizamos o flip quando terminamos de inserir os dados no buffer e
		// ele está pronto para ser lido
		buffer.flip();

		return buffer;

	}

	private FloatBuffer guardarDadosComoFloatBuffer(float[] dados) {
		// Criamos um buffer de float para podermos conseguir ler os dados do
		// VBO
		FloatBuffer buffer = BufferUtils.createFloatBuffer(dados.length);
		// Colocamos os dados passado como parâmetro no buffer
		buffer.put(dados);
		// Utilizamos o flip quando terminamos de inserir os dados no buffer e
		// ele está pronto para ser lido
		buffer.flip();

		return buffer;

	}

}
