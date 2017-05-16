class Config {}
Config.WIDTH = 800
Config.HEIGHT= 480
Config.DEBUG = true
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

		this.game.load.spritesheet('coin', Config.ASSETS + 'objects/Gold_1.png', 30, 30)
	}

	create() {
		this.game.physics.startSystem(Phaser.Physics.ARCADE)
        this.game.stage.backgroundColor = '#000000'
		
		let bg = this.game.add.tileSprite(0, 0, Config.WIDTH, Config.HEIGHT, 'background')
        bg.fixedToCamera = true

        this.keys = this.game.input.keyboard.createCursorKeys()
        this.game.physics.arcade.gravity.y = 550

        this.createMap()
        this.createPlayer()
        this.createCoins()
        this.createHud()

        let keys = this.game.input.keyboard.createCursorKeys()
	}

	createMap() {
		this.map = this.game.add.tilemap('level1')
        this.map.addTilesetImage('tiles')
        this.map.addTilesetImage('objects')

        this.mapLayer = this.map.createLayer('Tile Layer')

        this.map.setCollisionBetween(1, 18, true, 'Tile Layer')
        this.map.setCollisionBetween(236, 239, true, 'Tile Layer')
        this.map.setCollisionBetween(268, 271, true, 'Tile Layer')
        this.map.setCollisionBetween(300, 303, true, 'Tile Layer')
        // this.map.setCollisionBetween(6, 8, true, 'Tile Layer')
        // this.map.setCollisionBetween(10, 13, true, 'Tile Layer')
        // this.map.setCollisionBetween(16, 18, true, 'Tile Layer')
        this.mapLayer.resizeWorld()
	}

	createPlayer() {
		this.player = new Player(this.game, this.keys, 30, 900, 'character')
		this.game.add.existing(this.player)
		this.game.camera.follow(this.player, Phaser.Camera.FOLLOW_LOCKON, 0.1, 0.1)
	}

	createCoins() {
		this.coins = this.game.add.group()
		this.map.createFromObjects('Collection Layer', 467, 'coin', 0, true, false, this.coins, Coin)
	}

	createHud() {
		this.headImage = this.game.add.image(0, 0, 'head')
		this.headImage.fixedToCamera = true
		this.scoreText = this.game.add.text(60, 10, '', { fontSize: "16px", fill: '#ff0000' });
        this.scoreText.text = 'Irineu';
        this.scoreText.fixedToCamera = true; 
	}

	update() {
		this.game.physics.arcade.collide(this.player, this.mapLayer)
	}

	render() {
		if(Config.DEBUG) {
			this.game.debug.body(this.player)
		}
	}
}

const GAME = new Game()