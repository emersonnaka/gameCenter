class Config {}
Config.WIDTH = 800
Config.HEIGHT= 480
Config.DEBUG = false
Config.ANTIALIAS = true
Config.ASSETS = 'assets/'

class Game extends Phaser.Game {
    constructor() {
        super(Config.WIDTH, Config.HEIGHT, Phaser.CANVAS, 'game-container', null, false, Config.ANTIALIAS)

        this.state.add('Play', PlayState, false)
        this.state.start('Play')
    }
}

class PlayState extends Phaser.State {
	preload() {
		this.game.load.tilemap('level1', Config.ASSETS + 'phase1.json', null, Phaser.Tilemap.TILED_JSON)
		this.game.load.image('tiles', Config.ASSETS + 'tiles/tiles.png')
		this.game.load.image('objects', Config.ASSETS + 'objects/objects.png')
		this.game.load.image('background', Config.ASSETS + 'background.png')

		this.game.load.image('head', Config.ASSETS + 'character/Head.png')
		this.game.load.spritesheet('character', Config.ASSETS + 'character/character.png', 50, 60)

		this.game.load.spritesheet('coin', Config.ASSETS + 'objects/golds.png', 32, 32)
		this.game.load.spritesheet('lifes', Config.ASSETS + 'objects/lifes.png', 32, 32)

		this.game.load.image('trophy', Config.ASSETS + 'objects/trophy.png')
	}

	create() {
		this.game.physics.startSystem(Phaser.Physics.ARCADE)
        this.game.stage.backgroundColor = '#000000'
		
		let bg = this.game.add.tileSprite(0, 0, Config.WIDTH, Config.HEIGHT, 'background')
        bg.fixedToCamera = true

        this.keys = this.game.input.keyboard.createCursorKeys()
        this.game.physics.arcade.gravity.y = 550
        this.score = 0

        let fullScreenButton = this.game.input.keyboard.addKey(Phaser.Keyboard.ONE)
        fullScreenButton.onDown.add(this.toogleFullScreen, this)
        let screenshotButton = this.game.input.keyboard.addKey(Phaser.Keyboard.P)
        screenshotButton.onDown.add(this.takeScreenShot, this)

        this.createMap()
        this.createPlayer()
        this.createCollections()
        this.createHud()

        this.trophy = new Trophy(this.game)
        this.game.add.existing(this.trophy)
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
		this.player = new Player(this.game, this.keys, 30, 900, 'character')
		this.game.add.existing(this.player)
		this.game.camera.follow(this.player, Phaser.Camera.FOLLOW_LOCKON, 0.1, 0.1)
	}

	createCollections() {
		this.coins = this.game.add.group()
		this.map.createFromObjects('Collection Layer', 467, 'coin', 0, true, false, this.coins, Coin)

		this.life = this.game.add.group()
		this.map.createFromObjects('Collection Layer', 473, 'lifes', 0, true, false, this.life, Life)
	}

	createHud() {
		this.headImage = this.game.add.image(0, 0, 'head')
		this.headImage.fixedToCamera = true

		this.lifeText = this.game.add.text(60, 10, '', { fontSize: "16px", fill: '#ff0000' });
        this.lifeText.text = 'Irineu: 3';
        this.lifeText.fixedToCamera = true; 

        this.scoreText = this.game.add.text(60, 30, '', { fontSize: "16px", fill: '#000000' });
        this.scoreText.text = 'Coins: 0'
        this.scoreText.fixedToCamera = true
	}

	update() {
		this.game.physics.arcade.collide(this.player, this.mapLayer)

        this.game.physics.arcade.overlap(this.player, this.coins, this.collectCoin, null, this)

        this.game.physics.arcade.collide(this.player, this.trapsLayer, this.playerDied, null, this)

        this.game.physics.arcade.overlap(this.player, this.life, this.collectLife, null, this)
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
		this.player.x = 30
		this.player.y = 900
		this.camera.shake(0.02, 200)

		this.subLife(this.lifes)
	}

	subLife() {
		this.player.subLife()
		this.lifeText.text = 'Irineu: ' + this.player.lifes
	}

	render() {
		if(Config.DEBUG) {
			this.game.debug.body(this.player)
		}
	}
}

const GAME = new Game()