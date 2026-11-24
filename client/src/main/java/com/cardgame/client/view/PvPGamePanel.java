package com.cardgame.client.view;

import com.cardgame.client.view.components.CardTile;
import com.cardgame.client.view.components.RoundedPanel;
import com.cardgame.client.view.components.VectorIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import static com.cardgame.client.view.MainFrame.*;
import static com.cardgame.client.view.UiTheme.*;
public final class PvPGamePanel {
    private PvPGamePanel() {
    }
    public static JPanel create(MainFrame owner) {
        JPanel p = owner.page();
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel info = owner.vertical();
        info.add(owner.eyebrow("MEMORY DUEL  /  TRẬN ĐẤU"));
        info.add(Box.createVerticalStrut(4));
        info.add(owner.title("Đấu trí 1 VS 1", 24));
        owner.gameStatus = owner.subtitle("Đang chờ dữ liệu trận đấu...");
        info.add(owner.gameStatus);
        owner.playerNamesLabel = owner.subtitle("Bạn: —     |     Đối thủ: —");
        info.add(owner.playerNamesLabel);
        top.add(info, BorderLayout.WEST);
        owner.scoreLabel = owner.title("0   —   0", 27);
        owner.scoreLabel.setForeground(PURPLE_DARK);
        JPanel scoreInfo = new JPanel();
        scoreInfo.setOpaque(false);
        scoreInfo.setLayout(new BoxLayout(scoreInfo, BoxLayout.Y_AXIS));
        owner.scoreLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        owner.turnTimerLabel = owner.subtitle("Thời gian lượt: —");
        owner.turnTimerLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        scoreInfo.add(owner.scoreLabel);
        scoreInfo.add(owner.turnTimerLabel);
        top.add(scoreInfo, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(14,0));
        center.setOpaque(false);
        JPanel boardShell = owner.panel(WHITE);
        boardShell.setLayout(new BorderLayout(0,8));
        JPanel boardTitle = new JPanel(new BorderLayout());
        boardTitle.setOpaque(false);
        boardTitle.add(owner.title("Bàn chơi 6 × 6",16),BorderLayout.WEST);
        boardTitle.add(owner.subtitle("36 LÁ  •  18 CẶP"),BorderLayout.EAST);
        JPanel board = new JPanel(new GridLayout(6,6,7,7));
        board.setOpaque(false);
        for(int i=0;i<PVP_BOARD_SIZE;i++){
            final int index=i;
            CardTile b=new CardTile();
            b.setPreferredSize(new Dimension(88,96));
            b.setMinimumSize(new Dimension(58,62));
            b.addActionListener(e->{
                if(owner.selectingCardTarget){
                    if(owner.matched[index]||owner.revealed[index]||owner.locked[index]){
                        Toolkit.getDefaultToolkit().beep();return;
                    }
                    String code=owner.pendingCardUseCode; owner.selectingCardTarget=false; owner.pendingCardUseCode=""; owner.myTurn=false;
                    owner.send("USE_CARD","code",code,"index",index); owner.setTurnIndicator(true); owner.refreshBoardEnabled(); return;
                }
                if(owner.matched[index]||owner.revealed[index]||owner.locked[index])return;
                if(!owner.myTurn){
                    Toolkit.getDefaultToolkit().beep();return;
                }
                owner.send("FLIP","index",index);
            }
            );
            owner.cardButtons[i]=b;
            board.add(b);
        }
        JButton leave=owner.secondaryButton("RỜI TRẬN");
        leave.addActionListener(e->{
            int choice=JOptionPane.showConfirmDialog(owner.frame,"Bạn chắc chắn muốn rời trận?","Xác nhận",JOptionPane.YES_NO_OPTION);if(choice==JOptionPane.YES_OPTION)owner.send("LEAVE_MATCH");
        }
        );
        JPanel bottom=new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(leave,BorderLayout.EAST);
        boardShell.add(boardTitle,BorderLayout.NORTH);
        boardShell.add(board,BorderLayout.CENTER);
        boardShell.add(bottom,BorderLayout.SOUTH);
        JPanel side=owner.panel(new Color(250,247,255));
        side.setPreferredSize(new Dimension(245,500));
        side.setLayout(new BorderLayout(0,10));
        JLabel cardHeading=owner.title("THẺ ĐÃ CHỌN",16);
        side.add(cardHeading,BorderLayout.NORTH);
        owner.ownedCardPanel=new JPanel();
        owner.ownedCardPanel.setOpaque(false);
        owner.ownedCardPanel.setLayout(new BoxLayout(owner.ownedCardPanel,BoxLayout.Y_AXIS));
        JScrollPane ownedScroll=new JScrollPane(owner.ownedCardPanel);
        ownedScroll.setBorder(null);
        ownedScroll.getViewport().setOpaque(false);
        ownedScroll.setOpaque(false);
        side.add(ownedScroll,BorderLayout.CENTER);
        owner.skipCardButton=owner.secondaryButton("BỎ QUA THẺ");
        owner.skipCardButton.setVisible(false);
        owner.skipCardButton.addActionListener(e->{
            if(owner.cardPromptPhase.equals("END_TURN")&&!owner.availableCardCodes.isEmpty()){
                owner.availableCardCodes.clear();owner.selectingCardTarget=false;owner.pendingCardUseCode="";owner.myTurn=false;owner.send("SKIP_CARD");owner.renderPrivateCards();owner.refreshBoardEnabled();
            }
        }
        );
        JTextPane logArea=new JTextPane();
        owner.matchLogArea = logArea;
        logArea.setEditable(false);
        logArea.setFont(new Font("Segoe UI",Font.PLAIN,12));
        logArea.setBorder(new EmptyBorder(8,8,8,8));
        JPanel logPanel=new JPanel(new BorderLayout());
        logPanel.setOpaque(false);
        logPanel.add(owner.eyebrow("NHẬT KÝ TRẬN ĐẤU"),BorderLayout.NORTH);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(230, 150));
        logPanel.add(logScroll,BorderLayout.CENTER);
        JPanel sideBottom=new JPanel(new BorderLayout(0,8));
        sideBottom.setOpaque(false);
        sideBottom.add(owner.skipCardButton,BorderLayout.NORTH);
        sideBottom.add(logPanel,BorderLayout.CENTER);
        side.add(sideBottom,BorderLayout.SOUTH);
        center.add(boardShell,BorderLayout.CENTER);
        center.add(side,BorderLayout.EAST);
        p.add(center,BorderLayout.CENTER);
        return p;
    }
}
