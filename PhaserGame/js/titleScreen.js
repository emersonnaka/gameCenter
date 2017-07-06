// Tela de título
class TitleState extends Phaser.State {
    preload() {
        // mapa
        this.hasSave = false
        this.game.load.image('titleScreen', Config.ASSETS + 'titleScreen.jpg')
        this.game.load.image('title',Config.ASSETS + 'IrineusAdventure.png')
        ServerComm.sendCheckpoint(Config.USERNAME, Config.GAMENAME, 'load-state', 0, 0, 0,
            (response) => this.verifyState(response))
    }

    create() {
        super.create()

        this.bg = this.game.add.image(0, 0, 'titleScreen')
        this.bg.anchor.setTo(0, 0)

        this.imgTitle = this.game.add.image(0, 0, 'title')
        this.imgTitle.anchor.setTo(0.5, 0.5)
        this.imgTitle.x = this.game.width/2
        this.imgTitle.y = 150

        this.pressStart = this.game.add.text(0, 0, 'Press ENTER to begin', {fontSize: '16px', fill: '#000000'})
        this.pressStart.anchor.setTo(0.5, 0.5)
        this.pressStart.x = this.game.width/2
        this.pressStart.y = 300

        if(this.hasSave) {
            this.initSave = this.game.add.text(0, 0, 'Press P to continue', {fontSize: '16px', fill: '#000000'})
            this.initSave.anchor.setTo(0.5, 0.5)
            this.initSave.x = this.game.width/2
            this.initSave.y = 400

            let spaceButton = this.game.input.keyboard.addKey(
                Phaser.Keyboard.P)
            spaceButton.onDown.add(this.continueGame, this)
            this.hasSave = false
        }

        let fullScreenButton = this.game.input.keyboard.addKey(
            Phaser.Keyboard.ONE)
        fullScreenButton.onDown.add(this.toogleFullScreen, this)

        let startButton = this.game.input.keyboard.addKey(
            Phaser.Keyboard.ENTER)
        startButton.onDown.add(this.startFade, this)

        // fade no título
        this.imgTitle.alpha = 0.3
        this.game.add.tween(this.imgTitle).to({alpha: 1}, 2000).to({alpha: 0.3}, 2000).loop(-1).start()

        // fade no pressStart
        this.game.add.tween(this.pressStart).to({alpha: 0}, 500).to({alpha: 1}, 500).loop(-1).start()

        this.pressed = false
    }

    continueGame() {
        Config.X = this.xSave
        Config.Y = this.ySave
        Config.LEVEL = this.phaseSave
        this.startFade()
    }

    startFade() {
        if(!this.pressed) {
            this.pressed = true
            this.game.camera.fade(0x000000, 1000)
            this.game.camera.onFadeComplete.add(this.startGame, this)
        }
    }

    startGame() {
        // preparar o jogo
        //evitar bug de levar callback para outra tela (state)
        this.game.camera.onFadeComplete.removeAll(this)
        this.game.state.start('Play')
    }

    toogleFullScreen() {
        this.game.scale.fullScreenScaleMode = 
            Phaser.ScaleManager.EXACT_FIT;
        if (this.game.scale.isFullScreen)
            this.game.scale.stopFullScreen()
        else
            this.game.scale.startFullScreen(false)
    }

    verifyState(response) {
        if (response['response'] == 'ok') {
            this.hasSave = true
            this.xSave = response['data']['x']
            this.ySave = response['data']['y']
            this.phaseSave = response['data']['state']
            console.log(this.xSave)
            console.log(this.ySave)
            console.log(this.phaseSave)
        }
    }

    update() {

    }

}