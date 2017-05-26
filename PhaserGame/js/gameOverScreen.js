class GameOverState extends Phaser.State {
    preload() {
        // mapa
        this.game.load.image('gameOverScreen', Config.ASSETS + 'gameOver.png')
        this.game.load.image('headDead', Config.ASSETS + '/character/Head2.png')
    }

    create() {
        this.imgGameOver = this.game.add.image(0, 0, 'gameOverScreen')
        this.imgGameOver.anchor.setTo(0.5, 0.5)
        this.imgGameOver.x = this.game.width/2
        this.imgGameOver.y = 75

        this.bg = this.game.add.image(0, 0, 'headDead')
        this.bg.scale.setTo(0.5, 0.5)
        this.bg.anchor.setTo(0.5, 0.5)
        this.bg.x = this.game.width/2
        this.bg.y = 260

        this.pressStart = this.game.add.text(0, 0, 'Press ENTER to restart', {fontSize: '16px', fill: '#ffffff'})
        this.pressStart.anchor.setTo(0.5, 0.5)
        this.pressStart.x = this.game.width/2
        this.pressStart.y = 450

        let fullScreenButton = this.game.input.keyboard.addKey(
            Phaser.Keyboard.ONE)
        fullScreenButton.onDown.add(this.toogleFullScreen, this)

        let startButton = this.game.input.keyboard.addKey(
            Phaser.Keyboard.ENTER)
        startButton.onDown.add(this.startFade, this)

        // fade no título
        this.imgGameOver.alpha = 0.3
        this.game.add.tween(this.imgGameOver).to({alpha: 1}, 2000).to({alpha: 0.3}, 2000).loop(-1).start()

        // fade no pressStart
        this.game.add.tween(this.pressStart).to({alpha: 0}, 500).to({alpha: 1}, 500).loop(-1).start()

        this.pressed = false
    }

    startFade() {
        if(!this.pressed) {
            this.pressed = true
            this.game.camera.fade(0x000000, 1000)
            this.game.camera.onFadeComplete.add(this.restartGame, this)
        }
    }

    restartGame() {
        // preparar o jogo
        //evitar bug de levar callback para outra tela (state)
        this.game.camera.onFadeComplete.removeAll(this)
        this.game.state.start('Title')
    }

    toogleFullScreen() {
        this.game.scale.fullScreenScaleMode = 
            Phaser.ScaleManager.EXACT_FIT;
        if (this.game.scale.isFullScreen)
            this.game.scale.stopFullScreen()
        else
            this.game.scale.startFullScreen(false)
    }
}