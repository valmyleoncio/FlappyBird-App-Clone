package com.example.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {

	// Texturas
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canotopo;
	private Texture gameOver;

	// Formas Colisões
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	// Atributo de configurações
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal = 0;
	private float posicaoCanoVertical = 0;
	private float espacoEntreCanos = 0;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;

	// Exibição de textos
	private BitmapFont textoPontuacao;
	private BitmapFont textoReiniciar;
	private BitmapFont textoMelhorPontuação;

	// Configuraçao dos sons
	private Sound somVoando;
	private Sound somColisao;
	private Sound somPontuacao;

	// Objeto salvar pontuação
	private Preferences preferencias;

	// Objetos para câmera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT= 1280;

	@Override
	public void create () {

		inicializarTexturas();
		inicializarObjetos();

	}

	@Override
	public void render () {

		// Limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificaEstadoJogo();
		validarPontos();
		drawTexturas();
		detectarColisoes();

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose () {

	}



	private void inicializarObjetos(){

		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo  = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 300;

		// Configuração dos textos
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuação = new BitmapFont();
		textoMelhorPontuação.setColor(Color.RED);
		textoMelhorPontuação.getData().setScale(2);


		// Formas Geométricas para colisoes
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();


		// Inicializa sons
		somVoando    = Gdx.audio.newSound( Gdx.files.internal( "som_asa.wav" ));
		somColisao   = Gdx.audio.newSound( Gdx.files.internal( "som_batida.wav" ));
		somPontuacao = Gdx.audio.newSound( Gdx.files.internal( "som_pontos.wav" ));

		// Configura preferências dos objetos
		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);


		// Configuração da câmera
		camera = new OrthographicCamera();
		camera.position.set( VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0 );
		viewport = new StretchViewport( VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera );

	}

	private void inicializarTexturas(){

		passaros    = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");
		fundo       = new Texture("fundo.png");

		canoBaixo   = new Texture("cano_baixo_maior.png");
		canotopo    = new Texture("cano_topo_maior.png");

		gameOver    = new Texture("game_over.png");

	}

	private void drawTexturas(){

		batch.setProjectionMatrix( camera.combined );

		batch.begin();

		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo );
		batch.draw(passaros[(int) variacao] , 50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos /2 + posicaoCanoVertical);
		batch.draw(canotopo, posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf( pontos ), larguraDispositivo / 2 - textoPontuacao.getScaleX() , alturaDispositivo - 110 );

		if (estadoJogo == 2){

			batch.draw( gameOver, larguraDispositivo/2 - gameOver.getWidth() / 2, alturaDispositivo/2 );
			textoReiniciar.draw( batch, "Toque para reiniciar!", larguraDispositivo/2 - 140, alturaDispositivo/2 - gameOver.getHeight() / 2 );
			textoMelhorPontuação.draw(batch, "Seu record é: " + pontuacaoMaxima + " pontos", larguraDispositivo/2 - 140, alturaDispositivo/2 - gameOver.getHeight() - 10);

		}

		batch.end();

	}

	private void verificaEstadoJogo(){

		boolean toqueTela = Gdx.input.justTouched();

		if ( estadoJogo == 0 ){

			/* Aplica evento de toque */
			if ( toqueTela ){
				gravidade = - 15;
				estadoJogo = 1;
				somVoando.play();
			}

		}else if ( estadoJogo == 1 ){

			if ( toqueTela ){
				gravidade = - 15;
				somVoando.play();
			}

			/*  Movimentar o cano  */
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			if ( posicaoCanoHorizontal < -canotopo.getWidth() ){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(800) - 400;
				passouCano = false;
			}

			/* Aplica gravidade no pássaro */
			if ( posicaoInicialVerticalPassaro > 0 || gravidade < 0)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			gravidade++;

		}else if ( estadoJogo == 2 ){

			if ( pontos > pontuacaoMaxima ){
				pontuacaoMaxima = pontos;
				preferencias.putInteger( "pontuacaoMaxima", pontuacaoMaxima );
			}

			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;


			/* Aplica evento de toque */
			if ( toqueTela ){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo/2;
				posicaoHorizontalPassaro = 0;
				posicaoCanoHorizontal = larguraDispositivo;
			}

		}

		variacao += Gdx.graphics.getDeltaTime() * 10;

		/* Verifica variação pra bater asas */
		if (variacao > 3)
			variacao = 0;


	}

	private void validarPontos(){

		if (posicaoCanoHorizontal < -50){
			if ( !passouCano ){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

	}

	private void detectarColisoes(){

		circuloPassaro.set(50 + posicaoHorizontalPassaro  + passaros[0].getWidth() /2 ,posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2 ,passaros[0].getWidth() / 2);
		retanguloCanoBaixo.set( posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos /2 + posicaoCanoVertical, canoBaixo.getWidth(), canoBaixo.getHeight() );
		retanguloCanoCima.set( posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical, canotopo.getWidth(), canotopo.getHeight() );

		if (Intersector.overlaps( circuloPassaro, retanguloCanoCima ) || Intersector.overlaps( circuloPassaro, retanguloCanoBaixo )){

			if ( estadoJogo == 1 ){
				somColisao.play();
				estadoJogo = 2;
			}
		}

	}
}
