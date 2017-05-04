package executador;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import entidades.Camera;
import entidades.Entidade;
import entradas.MeuMouse;
import entradas.MeuTeclado;
import estruturasDeDados.Vetor3f;
import luminosidades.Difusa;
import objetos.CarregarObjeto;
import objetos.Objeto;
import objetos.ObjetoComTextura;
import renderizador.GerenciadorDeObjetos;
import renderizador.GerenciadorDeTempo;
import renderizador.Renderizador;
import terrenos.Terreno;
import renderizador.GerenciadorDeJanela;
import texturas.Textura;

public class Main {

	public static void main(String[] args) {

		//Preparações iniciais 
		GerenciadorDeJanela.criarDisplay();
		
		Camera camera = new Camera();
		
		Difusa luz = new Difusa(new Vetor3f(0,40,0),new Vetor3f(1,1,1));
		
		GerenciadorDeObjetos gerenciadorDeobj = new GerenciadorDeObjetos();
		 
		//Carrego o modelo do Objeto de uma fonte externa(e.g Blender)
		
		Objeto modeloDragao = CarregarObjeto.carregarObjeto("dragon",gerenciadorDeobj);
		Objeto modeloPlanta = CarregarObjeto.carregarObjeto("grassModel",gerenciadorDeobj);
		
		//Carrego a textura do Objeto de uma fonte externa(Deve ser .png)
		
		Textura texturaDragao = new Textura(gerenciadorDeobj.carregarTextura("dragonTexture"));
		Textura texturaPlanta = new Textura(gerenciadorDeobj.carregarTextura("grassTexture"));
	
		//Coloco o quanto a superficie da textura é reflexiva
		
		texturaDragao.setReflexo(10,1);
		
		//Coloco se a textura tem partes transparentes (Alpha)
		
		texturaPlanta.setTransparente(true);
		
		//Coloca se a textura precisa de uma iluminosidade Falsa
		
		texturaPlanta.setIluminosidadeFalsa(true);
		
		//Faço a conexão entre o Modelo do Objeto e a Textura dele
		
		ObjetoComTextura objetoDragao = new ObjetoComTextura(modeloDragao,texturaDragao);
		ObjetoComTextura objetoPlanta = new ObjetoComTextura(modeloPlanta,texturaPlanta); 
		
		//Faço a transformação dos Objetos e o transformo em Entidades
		
		List<Entidade> alcateia = new ArrayList<Entidade>();
		Random aleatorio = new Random();
		
		for(int i = 0; i < 2 ; i++){
			
			float x = aleatorio.nextFloat() * 50 - 25;
			float y = 0;
			float z = aleatorio.nextFloat() * - 150 -20;
			
			alcateia.add(new Entidade(objetoDragao,new Vetor3f(x, y, z),0f, 0f, 0f, 1f));
		}
		
		Entidade planta = new Entidade(objetoPlanta,new Vetor3f(0, 0, -30), 0f, 0f, 0f, 1f);

		//Criação de Terrenos
		
		Terreno terreno1 = new Terreno(0, -1, gerenciadorDeobj, new Textura(gerenciadorDeobj.carregarTextura("grass")));
		Terreno terreno2 = new Terreno(-1, -1, gerenciadorDeobj, new Textura(gerenciadorDeobj.carregarTextura("grass1")));
		Terreno terreno3 = new Terreno(-1, 0, gerenciadorDeobj, new Textura(gerenciadorDeobj.carregarTextura("grass2")));
		Terreno terreno4 = new Terreno(0, 0, gerenciadorDeobj, new Textura(gerenciadorDeobj.carregarTextura("grass3")));
		
		
		//Utiliza-se o GerenciadorDeTempo para fazer a medição de FPS
		
		GerenciadorDeTempo.getDelta(); // inicar o Delta
		GerenciadorDeTempo.setUltimoFPS(GerenciadorDeTempo.getTime());
		
		Renderizador renderizador = new Renderizador();
		
		//Loop principal da Engine que contém a lógica do Jogo
	
		while(!Display.isCloseRequested()){
			
			GerenciadorDeTempo.atualizarFPS();						
			
			camera.mover();
			
			renderizador.renderizar(luz, camera);
			
			//Renderizar Entidades
			
			for(Entidade dragaoObj : alcateia){
				dragaoObj.aumentarRotacao(0, 2, 0);
					renderizador.processarEntidades(dragaoObj);
			}
			
			renderizador.processarEntidades(planta);
			
			//Renderizar Terrenos
			
			renderizador.processarTerrenos(terreno1);
			renderizador.processarTerrenos(terreno2);
			renderizador.processarTerrenos(terreno3);
			renderizador.processarTerrenos(terreno4);
			
			

			MeuTeclado.tick();
			MeuMouse.tick();
			
			if(MeuTeclado.foiPressionada(Keyboard.KEY_UP))
				System.out.println("seta apertada");
			if(MeuTeclado.foiSolta(Keyboard.KEY_UP))
				System.out.println("seta solta");
			if(MeuMouse.foiPressionado(0))
				System.out.println("botao apertado hein" + MeuMouse.getMousePos().toString());
			if(MeuMouse.foiSolto(0))
				System.out.println("botao solto");
			
			GerenciadorDeJanela.atualizarDisplay();
		}		//Aqui somente será alcançado se fizermos uma requisição de fechar a janela

		renderizador.desalocar();
		gerenciadorDeobj.desalocar();

		GerenciadorDeJanela.fecharDisplay();
	}

}
