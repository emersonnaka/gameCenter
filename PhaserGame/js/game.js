class Config {}
Config.WIDTH = 800
Config.HEIGHT= 480
Config.DEBUG = false
Config.ANTIALIAS = true
Config.ASSETS = 'assets/'
Config.LEVEL = 2
Config.SCORE = 0
Config.GAMENAME = 'irineus-adventure'
Config.X = 30
Config.Y = 900
Config.USERNAME

class Game extends Phaser.Game {
    constructor() {
        super(Config.WIDTH, Config.HEIGHT, Phaser.CANVAS, 'game-container', null, false, Config.ANTIALIAS)

        this.state.add('Play', PlayState, false)
        this.state.add('Title', TitleState, false)
        this.state.add('GameOver', GameOverState, false)
        this.state.add('Win', WinState, false)
        this.state.start('Title')
    }
}

class GameState extends Phaser.State {
    create() {
        let fullScreenButton = this.input.keyboard.addKey(Phaser.Keyboard.ONE);
        fullScreenButton.onDown.add(this.toggleFullScreen, this)    

        this.scaleGame()
    }

    toggleFullScreen() {
        this.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
        if (this.scale.isFullScreen) {
            this.scale.stopFullScreen();
        } else {
            this.scale.startFullScreen(false);
        }
    }

    scaleGame() {
        this.game.scale.scaleMode = Phaser.ScaleManager.USER_SCALE //RESIZE

        // escala da tela        
        this.game.scale.setResizeCallback(function(scale, parentBounds) {
            //this.game.scale.setMaximum()
            let scaleX = Config.CONTAINER_WIDTH / Config.WIDTH
            let scaleY = Config.CONTAINER_HEIGHT / Config.HEIGHT

            this.game.scale.setUserScale(scaleX, scaleY, 0, 0)
        }, this)        
    }
}

class PlayState extends Phaser.State {
	preload() {
		this.game.load.tilemap('level1', Config.ASSETS + `phase${Config.LEVEL}.json`, null, Phaser.Tilemap.TILED_JSON)
		this.game.load.image('tiles', Config.ASSETS + 'tiles/tiles.png')
		this.game.load.image('objects', Config.ASSETS + 'objects/objects.png')
		this.game.load.image('background', Config.ASSETS + 'background.png')

		this.game.load.image('head', Config.ASSETS + 'character/HeadHUD.png')
		this.game.load.spritesheet('character', Config.ASSETS + 'character/character.png', 50, 60)

		this.game.load.spritesheet('coin', Config.ASSETS + 'objects/golds.png', 32, 32)
		this.game.load.spritesheet('lifes', Config.ASSETS + 'objects/lifes.png', 32, 32)
        this.game.load.spritesheet('checkpoint', Config.ASSETS + 'objects/Mushroom_2.png', 42, 42)
        this.game.load.spritesheet('snake', Config.ASSETS + 'objects/snake.png', 64, 32)

		this.game.load.image('trophy', Config.ASSETS + 'objects/trophy.png')
	}

	create() {
		super.create()

		this.game.physics.startSystem(Phaser.Physics.ARCADE)
        this.game.stage.backgroundColor = '#000000'
		
		let bg = this.game.add.tileSprite(0, 0, Config.WIDTH, Config.HEIGHT, 'background')
        bg.fixedToCamera = true

        this.keys = this.game.input.keyboard.createCursorKeys()
        this.game.physics.arcade.gravity.y = 550
        this.score = 0

        let screenshotButton = this.game.input.keyboard.addKey(Phaser.Keyboard.P)
        screenshotButton.onDown.add(this.takeScreenShot, this)

        this.createMap()
        this.createPlayer()
        this.createCollections()
        this.createEnemies()
        this.createHud()

        this.trophy = new Trophy(this.game)
        this.game.add.existing(this.trophy)

        this.game.camera.flash(0x000000, 1000)
        this.map.setTileIndexCallback(240, this.loadNextLevel, this)
        this.levelCleared = false
        this.addScore(0)
	}

	toogleFullScreen() {
		this.game.scale.fillScreenScaleMode = Phaser.ScaleManager.EXACT_FIT
		if(this.game.scale.isFullScreen)
			this.game.scale.stopFullScreen()
		else
			this.game.scale.startFullScreen(false)
	}

	takeScreenShot() {
		let imgData = this.game.canvas.toDataURL()
        $('#div-screenshot').append(`<img src=${imgData} alt='game screenshot' class='screenshot'>`)
        console.log('cade o print?')
	}

	createMap() {
		this.map = this.game.add.tilemap('level1')
        this.map.addTilesetImage('tiles')
        this.map.addTilesetImage('objects')
        this.map.addTilesetImage('checkpoint')

        this.mapLayer = this.map.createLayer('Tile Layer')

        this.map.setCollisionBetween(1, 13, true, 'Tile Layer')
        this.map.setCollisionBetween(16, 18, true, 'Tile Layer')
        this.map.setCollisionBetween(237, 239, true, 'Tile Layer')
        this.map.setCollisionBetween(269, 271, true, 'Tile Layer')
        this.map.setCollisionBetween(301, 303, true, 'Tile Layer')

        this.mapLayer.resizeWorld()

        this.trapsLayer = this.map.createLayer('Trap Layer')
        this.map.setCollision([14], true, 'Trap Layer')
        this.map.setCollision([19], true, 'Trap Layer')
	}

	createPlayer() {
        this.xSave = Config.X
        this.ySave = Config.Y
		this.player = new Player(this.game, this.keys, this.xSave, this.ySave, 'character')
		this.game.add.existing(this.player)
		this.game.camera.follow(this.player, Phaser.Camera.FOLLOW_LOCKON, 0.1, 0.1)
	}

	createCollections() {
		this.coins = this.game.add.group()
		this.map.createFromObjects('Collection Layer', 467, 'coin', 0, true, false, this.coins, Coin)

		this.life = this.game.add.group()
		this.map.createFromObjects('Collection Layer', 473, 'lifes', 0, true, false, this.life, Life)

        this.check = this.game.add.group()
        this.map.createFromObjects('Collection Layer', 479, 'checkpoint', 0, true, false, this.check, Checkpoint)
	}

    createEnemies() {
        this.snakes = this.game.add.group()
        this.map.createFromObjects('Collection Layer', 483, 'snake', 0, true, false, this.snakes, Snake)
        this.snakes.forEach( (Snake) => Snake.start() )
    }

	createHud() {
		this.headImage = this.game.add.image(0, 0, 'head')
		this.headImage.fixedToCamera = true

		this.lifeText = this.game.add.text(60, 10, '', { fontSize: "16px", fill: '#ff0000' })
        this.lifeText.text = 'Irineu: ' + this.player.lifes
        this.lifeText.fixedToCamera = true

        this.scoreText = this.game.add.text(60, 30, '', { fontSize: "16px", fill: '#000000' })
        this.scoreText.text = 'Coins: 0'
        this.scoreText.fixedToCamera = true
	}

	update() {
		this.game.physics.arcade.collide(this.player, this.mapLayer)

        this.game.physics.arcade.overlap(this.player, this.coins, this.collectCoin, null, this)

        this.game.physics.arcade.collide(this.player, this.trapsLayer, this.playerDied, null, this)

        this.game.physics.arcade.overlap(this.player, this.life, this.collectLife, null, this)

        this.game.physics.arcade.overlap(this.player, this.check, this.checkpoint, null, this)

        this.game.physics.arcade.overlap(this.player, this.snakes, this.playerDied, null, this)
	}

	collectCoin(player, coin) {
		coin.destroy()
		this.addScore(coin.points)
		if(this.score == 10)
			this.trophy.show('ten coins')
	}

	addScore(amount) {
		this.score += amount
		this.scoreText.text = 'Coins: ' + this.score
	}

	collectLife(player, life) {
		life.destroy()
		this.addLife()
	}

	addLife(amount) {
		this.player.addLife()
		this.lifeText.text = 'Irineu: ' + this.player.lifes
	}

	playerDied() {
		this.camera.shake(0.02, 200)

		this.subLife(this.lifes)
		this.trophy.show('first death')

		if(this.player.lifes < 0) {
			this.game.camera.onFadeComplete.removeAll(this)
        	this.game.state.start('GameOver')
        } else {
            this.player.x = this.xSave
            this.player.y = this.ySave
        }
	}

	subLife() {
		this.player.subLife()
		this.lifeText.text = 'Irineu: ' + this.player.lifes
	}

    checkpoint(player, check) {
        this.xSave = check.x
        this.ySave = check.y
        check.destroy()
        
        ServerComm.sendCheckpoint('luisao', Config.GAMENAME, 'save-state', this.xSave, this.ySave, Config.LEVEL,
            (response) => this.onServerResponse(response))
    }

    onServerResponse(response) {
        if (response['response'] != 'ok') {
            console.log("ERRO de comunicao com o servidor")
            return
        }
        console.log('save-state')
    }

	loadNextLevel() {
        if (!this.levelCleared) {
            this.levelCleared = true
            this.game.camera.fade(0x000000, 1000)
            this.game.camera.onFadeComplete.add(this.changeLevel, this)
        }
    }

    changeLevel() {
        Config.LEVEL += 1
        Config.SCORE = this.score
        this.game.camera.onFadeComplete.removeAll(this)// bug
        if (Config.LEVEL <= 3)
            this.game.state.restart()
        else
            this.game.state.start('Win')
    }

	render() {
		if(Config.DEBUG) {
			this.game.debug.body(this.player)
		}
	}
}

const GAME = new Game()