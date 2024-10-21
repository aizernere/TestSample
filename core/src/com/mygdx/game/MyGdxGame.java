package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.audio.Music;

public class MyGdxGame extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture spriteSheet, background, shadow, metalingSheet;
	private Music backgroundMusic;
	private Animation<TextureRegion> walkDownAnimation, walkUpAnimation, walkLeftAnimation, walkRightAnimation, metalingIdleAnimation;
	private Vector2 characterPosition, targetPosition, monsterPosition;
	private float speed = 200f;
	private float animationTime = 0f;
	private int currentState = 0;

	private int[][] spriteMovement = {
			// click down, click right, click left, click up, reached target
			{4, 5, 6, 7, 0}, // idle down
			{4, 5, 6, 7, 1}, // idle right
			{4, 5, 6, 7, 2}, // idle left
			{4, 5, 6, 7, 3}, // idle up
			{4, 5, 6, 7, 0}, // move down
			{4, 5, 6, 7, 1}, // move right
			{4, 5, 6, 7, 2}, // move left
			{4, 5, 6, 7, 3}  // move up

	};

	@Override
	public void create() {
		batch = new SpriteBatch();
		spriteSheet = new Texture("character.png");
		background = new Texture("background.png");
		shadow = new Texture("shadow.png");
		metalingSheet = new Texture("metaling.png");

		TextureRegion[][] tmpFrames = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 9, spriteSheet.getHeight() / 4);

		walkDownAnimation = new Animation<>(0.1f, tmpFrames[0]);
		walkRightAnimation = new Animation<>(0.1f, tmpFrames[1]);
		walkLeftAnimation = new Animation<>(0.1f, tmpFrames[2]);
		walkUpAnimation = new Animation<>(0.1f, tmpFrames[3]);

		characterPosition = new Vector2(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		targetPosition = new Vector2(characterPosition);

		//music
		backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("bgMusic.mp3"));
		backgroundMusic.setLooping(true);
		backgroundMusic.setVolume(0.1f);
		backgroundMusic.play();

		//idle monster
		TextureRegion[][] monsterTmpFrames = TextureRegion.split(metalingSheet, metalingSheet.getWidth() / 4, metalingSheet.getHeight());
		metalingIdleAnimation = new Animation<>(0.1f, monsterTmpFrames[0]);
		metalingIdleAnimation.setPlayMode(Animation.PlayMode.LOOP);
		monsterPosition = new Vector2(Gdx.graphics.getWidth() - 400, 350);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		animationTime += Gdx.graphics.getDeltaTime();
		TextureRegion currentMonsterFrame = metalingIdleAnimation.getKeyFrame(animationTime, true);

		// Handle input for target position
		if (Gdx.input.isTouched()) {
			targetPosition.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
			Vector2 direction = new Vector2(targetPosition).sub(characterPosition);
			int input = -1;
			//input
			if (Math.abs(direction.x) > Math.abs(direction.y)) {
				if (direction.x > 0) input = 1; // right
				else input = 2; 				// left
			} else {
				if (direction.y > 0) input = 3; // up
				 else input = 0; 				// down
			}
			currentState = spriteMovement[currentState][input];
		}


		float stoppingThreshold = 3f;
		if (characterPosition.dst(targetPosition) > stoppingThreshold) {
			Vector2 direction = targetPosition.cpy().sub(characterPosition).nor();
			characterPosition.add(direction.scl(speed * Gdx.graphics.getDeltaTime()));
		} else {
			characterPosition.set(targetPosition);
			currentState = currentState % 4; // Switch to idle state (0-3) to break animation
		}

		// Select the animation based on the current state
		TextureRegion currentFrame = null;
		switch (currentState) {
			case 0: // Idle down
				currentFrame = walkDownAnimation.getKeyFrames()[0];
				break;
			case 1: // Idle right
				currentFrame = walkRightAnimation.getKeyFrames()[0];
				break;
			case 2: // Idle left
				currentFrame = walkLeftAnimation.getKeyFrames()[0];
				break;
			case 3: // Idle up
				currentFrame = walkUpAnimation.getKeyFrames()[0];
				break;
			case 4: // Move down
				currentFrame = walkDownAnimation.getKeyFrame(animationTime, true);
				break;
			case 5: // Move right
				currentFrame = walkRightAnimation.getKeyFrame(animationTime, true);
				break;
			case 6: // Move left
				currentFrame = walkLeftAnimation.getKeyFrame(animationTime, true);
				break;
			case 7: // Move up
				currentFrame = walkUpAnimation.getKeyFrame(animationTime, true);
				break;
		}


		// Draw bg, shadow, char
		float spriteWidth = currentFrame.getRegionWidth();
		float adjustedX = characterPosition.x - spriteWidth / 2;
		float adjustedY = characterPosition.y - 10;
		batch.begin();
		batch.draw(background, 0, 0, 1337, 730);
		batch.setColor(0f, 0f, 0f, 0.5f);
		if (currentFrame != null) {
			batch.draw(shadow, adjustedX + 15, adjustedY + 10, shadow.getWidth(), shadow.getHeight());
		}
		if (currentMonsterFrame != null) {
			batch.draw(shadow, monsterPosition.x + 5, monsterPosition.y + 10, shadow.getWidth(), shadow.getHeight());
		}

		//MOCK HP/SP BAR
		batch.setColor(0f, 0.75f, 0f, 1f);
		batch.draw(new Texture("pixel.png"), adjustedX + 5, adjustedY, 70, 4);
		batch.setColor(0f, 0f, 1f, 1f);
		batch.draw(new Texture("pixel.png"), adjustedX + 5, adjustedY - 4,  70, 4);

		// draw character
		batch.setColor(1f, 1f, 1f, 1f);
		if (currentFrame != null) {
			batch.draw(currentFrame, adjustedX,adjustedY);
		}

		//draw monster
		batch.draw(currentMonsterFrame, monsterPosition.x, monsterPosition.y);

		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
		spriteSheet.dispose();
		background.dispose();
		shadow.dispose();
		metalingSheet.dispose();
	}
}
