package com.chairbender.yomi.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.chairbender.yomi.api.card.Card;
import com.chairbender.yomi.api.card.move.*;
import com.chairbender.yomi.api.character.YomiCharacter;

/**
 * Represents a card. The position of this should be set by the parent, but the width should not be modified.
 * Position is relative to the bottom left
 */
public class CardGroup extends Group {


    private Card card;

    private Image cardImage;
    private float scale = 2.2f;

    private MoveInfoGroup topMove;
    private MoveInfoGroup bottomMove;

    private float rotation = 0;

    /**
     *
     * @param card the card this actor should represent.
     */
    public CardGroup(Card card) {
        this.card = card;

        //this card's position are determined by the parent, but height is determined by this
        //actor


        //create everything used in the draw method
        Texture cardTexture = new Texture(getImageFileName());
        this.cardImage = new Image(cardTexture);
        cardImage.setBounds(0,0,cardTexture.getWidth()*scale,cardTexture.getHeight()*scale);
        //add the move info
        topMove = new MoveInfoGroup(card.topMoveInfo(),this);
        bottomMove = new MoveInfoGroup(card.bottomMoveInfo(),this);
        addActor(cardImage);
        addActor(topMove);
        addActor(bottomMove);

        positionEverything();

        //click event
        cardImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                rotate();
                return true;
            }
        });

    }

    /**
     * position everything based on this group's coordinates and bounds
     */
    private void positionEverything() {
        topMove.setPosition(27,cardImage.getHeight() - topMove.getHeight());
        bottomMove.setPosition(27,cardImage.getHeight() - bottomMove.getHeight());
        //set origins to this card's middle point
        this.setOrigin(getWidth()/2,getHeight()/2);
        topMove.setOrigin(getWidth()/2 - topMove.getX(), getHeight()/2 - topMove.getY());
        //need 45 and -50
        bottomMove.setOrigin(getWidth()/2 - bottomMove.getX(), getHeight()/2 - bottomMove.getY());

        bottomMove.setRotation(rotation + 180);
        topMove.setRotation(rotation);
    }



    /**
     * rotate the card so the other side is on top, via an animation
     */
    public void rotate() {
        this.addAction(Actions.rotateBy(180,0.5f, Interpolation.pow4));
    }

    @Override
    public float getHeight() {
        return cardImage.getHeight();
    }

    @Override
    public float getWidth() {
        return cardImage.getWidth();
    }

    /**
     * set the position of this card
     * @param x
     * @param y
     */
    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        positionEverything();

    }

    /* @Override
    public void draw(Batch batch, float alpha) {
        cardSprite.setBounds(getX(),getY(),scale * cardSprite.getTexture().getWidth(),
                scale * cardSprite.getTexture().getHeight());
        cardSprite.draw(batch);
    }*/

    /**
     *
     * @return the string that is the name of the image file (including cards/ before it)
     * to use for this card's
     * base poker card image.
     */
    private String getImageFileName() {
        if (card.isJoker()) {
            return "cards/53.png";
        }
        PokerValue value = card.getValue();
        int suitOffset = 0;
        if (value.getSuit().equals(PokerValue.Suit.CLUBS)) {
            suitOffset = 0;
        } else if (value.getSuit().equals(PokerValue.Suit.SPADES)) {
            suitOffset = 1;
        } else if (value.getSuit().equals(PokerValue.Suit.HEARTS)) {
            suitOffset = 2;
        } else {
            suitOffset = 3;
        }

        PokerRank rank = value.getRank();
        rank.getIntegerValue();
        int startingNumber =  53 - (rank.getIntegerValue() - 1)*4;

        return "cards/" + (startingNumber + suitOffset) + ".png";

    }
}

/**
 * group for drawing move info
 */
class MoveInfoGroup extends Group {
    private static final float MOVE_SPACING = 0;
    private final MoveInfo move;

    private static final float moveNameScale = 0.8f;
    private static final float iconScale = 0.8f;
    private static final float blockDamageIconScale = 0.4f;
    private Label moveTypeLabel;

    private SpeedBoxGroup speedBoxGroup;
    private Image comboBox;

    private Image comboTypeBox;

    private Image knockDownBox;

    private Image pumpCostBox;
    private Label pumpCostLabel;

    private Image moveTypeIcon;
    private Label damageLabel;

    private Label blockDamageLabel;
    private Image blockDamageImage;
    private static final Texture blockTexture = new Texture("icons/block.png");




    public MoveInfoGroup(MoveInfo move,CardGroup parent) {
        this.move = move;

        String moveTypeString = "";
        String moveTypeIconFile = "";
        String damageString = "";
        String blockDamageString = null;
        Color typeColor;
        //TODO: Reuse the move icon textures
        if (move instanceof OffensiveMoveInfo) {
            OffensiveMoveInfo offense = (OffensiveMoveInfo) move;
            if (offense.getMoveType().equals(MoveType.ATTACK)) {
                moveTypeString += "Attack\n";
                moveTypeIconFile = "icons/attack.png";
                typeColor = Color.RED;
            } else {
                moveTypeString += "Throw\n";
                typeColor = Color.BLACK;
                moveTypeIconFile = "icons/throw.png";
            }
            if (offense.getBlockDamage() > 0) {
                blockDamageString = offense.getBlockDamage() + "";
            }
            damageString = offense.getDamage() + "";
            if (offense.getPumpDamage() > 0) {
                damageString += "+" + offense.getPumpDamage();
            }
            if (!(offense.getComboType().equals(ComboType.CANTCOMBO) ||
                    offense.getComboType().equals(ComboType.NORMAL))) {

            }
            if (offense.hasKnockdown()) {

            }
            if (offense.isPumpable()) {
                String pumpString = "";
                for (int i = 0; i < offense.getPumpLimit(); i++) {
                    pumpString += "+" + offense.getPumpRank().toString();
                }
            }

            speedBoxGroup = new SpeedBoxGroup(offense.getSpeed());
            speedBoxGroup.setScale(0.6f);
            addActor(speedBoxGroup);

            ComboBoxGroup comboBoxGroup = new ComboBoxGroup(offense.getComboPoints(),move.getParentCard().getCharacter());
            comboBoxGroup.setPosition(speedBoxGroup.getX() + speedBoxGroup.getWidth(),0);
            comboBoxGroup.setScale(0.6f);
            addActor(comboBoxGroup);




        } else {
            if (move.getMoveType().equals(MoveType.BLOCK)) {
                moveTypeString += "Block";
                typeColor = Color.BLUE;
                moveTypeIconFile = "icons/block.png";
            } else {
                moveTypeString += "Dodge";
                typeColor = Color.PURPLE;
                moveTypeIconFile = "icons/dodge.png";
            }
        }


        //TODO: position move type text above speedboxgroup

        //The move type text (Attack, Block,...)
        moveTypeLabel   = new Label(moveTypeString, new Label.LabelStyle(new BitmapFont(),typeColor));
        if (speedBoxGroup != null) {
            moveTypeLabel.setPosition(0,speedBoxGroup.getHeight() + speedBoxGroup.getY() + MOVE_SPACING);
        }
        moveTypeLabel.setFontScale(moveNameScale);
        addActor(moveTypeLabel);

        Texture moveIcon = new Texture(moveTypeIconFile);
        moveTypeIcon = new Image(moveIcon);
        damageLabel = new Label(damageString, new Label.LabelStyle(new BitmapFont(),Color.WHITE));
        positionDamageIcon(moveTypeIcon, damageLabel,moveTypeLabel.getX() + 88,moveTypeLabel.getY() - 20, iconScale);

        addActor(moveTypeIcon);
        addActor(damageLabel);

        //handle block damage
        if (blockDamageString != null) {
            blockDamageImage = new Image(blockTexture);
            blockDamageLabel = new Label(blockDamageString, new Label.LabelStyle(new BitmapFont(),Color.WHITE));
            blockDamageLabel.setFontScale(.8f);
            positionDamageIcon(blockDamageImage, blockDamageLabel,
                    moveTypeIcon.getX() + moveTypeIcon.getWidth() - blockDamageImage.getWidth() / 2,
                    moveTypeIcon.getY() - (blockDamageImage.getHeight() * blockDamageIconScale) / 2, blockDamageIconScale);

            addActor(blockDamageImage);
            addActor(blockDamageLabel);

        }
    }

    private void positionDamageIcon(Image icon, Label damage,float x, float y, float iconScale) {
        //position relative to move label
        icon.setPosition(x,y);
        icon.setWidth(icon.getWidth()*iconScale);
        icon.setHeight(icon.getHeight() * iconScale);
        damage.setPosition(icon.getX(),icon.getY());
        damage.setCenterPosition(icon.getCenterX(),icon.getCenterY());
    }

    @Override
    public float getHeight() {
        return moveTypeLabel.getHeight() + (speedBoxGroup != null ? speedBoxGroup.getHeight() + MOVE_SPACING: 0);
    }
}

class SpeedBoxGroup extends Group {
    private static final Texture speedTexture = new Texture("icons/speed.png");
    public SpeedBoxGroup(Speed speed) {
        Image speedBox = new Image(speedTexture);
        float speedScale = 0.6f;
        this.setWidth(speedTexture.getWidth()*speedScale);
        this.setHeight(speedTexture.getHeight()*speedScale);
        Label speedLabel = new Label(speed+ "", new Label.LabelStyle(new BitmapFont(),Color.BLACK));
        speedLabel.setPosition(speedBox.getX() + 23,speedBox.getY() - 1);
        speedLabel.setFontScale(0.8f);
        addActor(speedBox);
        addActor(speedLabel);
    }
}

class ComboBoxGroup extends Group {
    private static final Texture comboTexture = new Texture("icons/combo_points.png");
    private static final Texture filledTexture = new Texture("icons/combo_filled.png");
    private static final Texture emptyTexture = new Texture("icons/combo_empty.png");
    private static final int HORIZ_SPACING = 2;

    public ComboBoxGroup(int comboPoints, YomiCharacter character) {
        Image box = new Image(comboTexture);
        addActor(box);

        //Fill it
        for (int i = 0; i < character.getMaxCombo(); i++) {
            //first line
            Image nextStone = comboPoints < i ? new Image(emptyTexture) : new Image(filledTexture);
            if (i <= 2) {
                //first line
                nextStone.setPosition(i * (HORIZ_SPACING + filledTexture.getWidth()) + 18,
                        8);
            } else {
                //second line
                nextStone.setPosition((i - 3) * (HORIZ_SPACING + filledTexture.getWidth())  + 18,
                        2);
            }
            addActor(nextStone);
        }


    }
}