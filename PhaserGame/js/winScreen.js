class WinState extends Phaser.State {
	preload() {
		this.game.load.image('titleScreen', Config.ASSETS + 'titleScreen.jpg')
		this.game.load.image('headWin', Config.ASSETS + '/character/Head.png')
		this.game.load.image('youWin', Config.ASSETS + 'youWin.png')
	}

	create() {
        super.create()
		this.bg = this.game.add.image(0, 0, 'titleScreen')
		this.bg.anchor.setTo(0.2, 0.3)

		this.imgWin = this.game.add.image(0, 0, 'youWin')
		this.imgWin.anchor.setTo(0.5, 0.5)
		this.imgWin.x = this.game.width/2
        this.imgWin.y = 75

        this.imgHead = this.game.add.image(0, 0, 'headWin')
        this.imgHead.scale.setTo(0.6, 0.6)
        this.imgHead.anchor.setTo(0.5, 0.5)
        this.imgHead.x = this.game.width/2
        this.imgHead.y = this.game.height/2


		this.pressStart = this.game.add.text(0, 0, 'Press ENTER to begin again', {fontSize: '16px', fill: '#000000'})
        this.pressStart.anchor.setTo(0.5, 0.5)
        this.pressStart.x = this.game.width/2
        this.pressStart.y = 400

        let fullScreenButton = this.game.input.keyboard.addKey(
            Phaser.Keyboard.ONE)
        fullScreenButton.onDown.add(this.toogleFullScreen, this)

        let restartButton = this.game.input.keyboard.addKey(
            Phaser.Keyboard.ENTER)
        restartButton.onDown.add(this.startFade, this)

        this.imgWin.alpha = 0.3
        this.game.add.tween(this.imgWin).to({alpha: 1}, 300).to({alpha: 0.3}, 300).loop(-1).start()

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

	toogleFullScreen() {
		this.game.scale.fillScreenScaleMode = Phaser.ScaleManager.EXACT_FIT
		if(this.game.scale.isFullScreen)
			this.game.scale.stopFullScreen()
		else
			this.game.scale.startFullScreen(false)
	}

	restartGame() {
        // preparar o jogo
        //evitar bug de levar callback para outra tela (state)
        this.game.camera.onFadeComplete.removeAll(this)
        this.game.state.start('Title')
    }
}