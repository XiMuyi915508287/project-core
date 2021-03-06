package com.project.core.battle.control.common;

import com.game.common.util.ResultCode;
import com.game.common.util.TupleCode;
import com.project.core.battle.Battle;
import com.project.core.battle.BattleContext;
import com.project.core.battle.BattleStage;
import com.project.core.battle.BattleTeamType;
import com.project.core.battle.BattleTeamUnit;
import com.project.core.battle.BattleUnit;
import com.project.core.battle.control.BattleControlId;
import com.project.core.battle.control.BattleControl_Command;

import java.util.List;

/**
 * 一支队伍
 * @param <T> 请求的参数
 * @param <E> 处理的参数
 */
public abstract class BattleControl_TeamUnit<T, E> extends BattleControl_Command<T, E> {

	protected final int teamUnitIndex;

	public BattleControl_TeamUnit(BattleControlId battleControlId, BattleStage battleStage, int teamUnitIndex) {
		super(battleControlId, battleStage.crateChild(teamUnitIndex));
		this.teamUnitIndex = teamUnitIndex;
	}

	protected final BattleTeamType getTeamType(){
		return BattleTeamType.getIndexTeamType(teamUnitIndex);
	}

	@Override
	protected final TupleCode<E> getExecuteCommand(BattleContext battleContext, T requestCommand) {
		Battle battle = battleContext.getBattle();
		BattleTeamUnit battleTeamUnit = battle.getBattleUnitManager().getIndexBattleTeamUnit(teamUnitIndex);
		if (!battleTeamUnit.getAllUnitUserIds().contains(battleContext.getOperateUserId())) {
			return new TupleCode<>(ResultCode.BATTLE_CONTROL_NOT_SUPPORT);
		}
		return getTeamUnitCommand(battleContext, requestCommand);
	}

	protected abstract TupleCode<E> getTeamUnitCommand(BattleContext battleContext, T requestCommand);

	@Override
	protected final boolean executeCommand(BattleContext battleContext) {
		Battle battle = battleContext.getBattle();
		BattleTeamUnit battleTeamUnit = battle.getBattleUnitManager().getIndexBattleTeamUnit(teamUnitIndex);
		List<BattleUnit> battleUnitList = battleTeamUnit.getBattleUnitList(BattleUnit::isAlive);
		for (BattleUnit battleUnit : battleUnitList) {
			if (battleUnit.getUserId() <= 0) {
				continue;
			}
			if (!battle.getOperateManager().containerOperate(battleUnit.getUserId())){
				return false;
			}
		}
		return executeTeamUnit(battleContext, battleTeamUnit);
	}

	protected abstract boolean executeTeamUnit(BattleContext battleContext, BattleTeamUnit teamUnit);
}
