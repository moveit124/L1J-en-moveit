package l1j.server.server.model;

import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_PARALYZED;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_PARALYZING;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.server.GeneralThreadPool;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_ServerMessage;

public class L1CurseParalysis extends L1Paralysis {
    private static Logger _log = LoggerFactory.getLogger(L1CurseParalysis.class);

    private final L1Character _target;
    private final int _delay;
    private final int _time;

    private Runnable _timer;
    private ScheduledFuture<?> _timerFuture;

    private class ParalysisTransition implements Runnable {
        @Override
        public void run() {
            try {
                // Only continue if paralyzing effect is still active
                if (!_target.hasSkillEffect(STATUS_CURSE_PARALYZING)) {
                    return;
                }

                if (_target instanceof L1PcInstance) {
                    L1PcInstance player = (L1PcInstance) _target;
                    if (!player.isDead()) {
                        player.sendPackets(new S_Paralysis(1, true));
                    }
                }
                _target.setParalyzed(true);

                _timer = new ParalysisTimer();
                _timerFuture = GeneralThreadPool.getInstance().schedule(_timer, _time);
            } catch (Exception e) {
                _log.error("", e);
            }
        }
    }

    private class ParalysisTimer implements Runnable {
        @Override
        public void run() {
            _target.killSkillEffectTimer(STATUS_CURSE_PARALYZING);
            _target.setSkillEffect(STATUS_CURSE_PARALYZED, 0);
            _target.killSkillEffectTimer(STATUS_CURSE_PARALYZED);

            if (_target instanceof L1PcInstance) {
                L1PcInstance player = (L1PcInstance) _target;
                if (!player.isDead()) {
                    player.sendPackets(new S_Paralysis(1, false));
                }
            }
            _target.setParalyzed(false);
            cure();
        }
    }

    private L1CurseParalysis(L1Character cha, int delay, int time) {
        _target = cha;
        _delay = delay;
        _time = time;

        curse();
    }

    private void curse() {
        if (_target instanceof L1PcInstance) {
            L1PcInstance player = (L1PcInstance) _target;
            player.sendPackets(new S_ServerMessage(212));
        }

        _target.setPoisonEffect(2);
        _target.setSkillEffect(STATUS_CURSE_PARALYZING, 0);

        _timer = new ParalysisTransition();
        _timerFuture = GeneralThreadPool.getInstance().schedule(_timer, _delay);
    }

    public static boolean curse(L1Character cha, int delay, int time) {
        if (!(cha instanceof L1PcInstance || cha instanceof L1MonsterInstance)) {
            return false;
        }
        if (cha.hasSkillEffect(STATUS_CURSE_PARALYZING) || cha.hasSkillEffect(STATUS_CURSE_PARALYZED)) {
            return false;
        }

        cha.setParalaysis(new L1CurseParalysis(cha, delay, time));
        return true;
    }

    @Override
    public int getEffectId() {
        return 2;
    }

    @Override
    public void cure() {
        if (_timerFuture != null) {
            try {
                _timerFuture.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        _timer = null;

        _target.setSkillEffect(STATUS_CURSE_PARALYZING, 0);
        _target.killSkillEffectTimer(STATUS_CURSE_PARALYZING);
        _target.setSkillEffect(STATUS_CURSE_PARALYZED, 0);
        _target.killSkillEffectTimer(STATUS_CURSE_PARALYZED);

        if (_target instanceof L1PcInstance) {
            L1PcInstance player = (L1PcInstance) _target;
            if (!player.isDead()) {
                player.sendPackets(new S_Paralysis(1, false));
            }
        }

        _target.setParalyzed(false);
        _target.setPoisonEffect(0);
        _target.setParalaysis(null);
    }
}
