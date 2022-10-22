import { IOperation, jot, OTActionName } from 'engine/ot';
import { isEqual } from 'lodash';

/**
 * Compose (Merge) operations
 * Only compose for operations that same path and single action's operation 
 * 
 * @param operations Operations Array
 */
export function composeOperations(operations: IOperation[]): IOperation[] {
  return operations.reduce<IOperation[]>((pre, cur) => {
    delete cur.resourceType;
    delete cur.revision;
    delete cur.fieldTypeMap;
    if (pre.length) {
      const preOp = pre.pop();
      const { operation, isComposed } = composeOperation(cur, preOp!);
      if (operation != null) {
        pre.push(operation);
        if (!isComposed) {
          pre.push(cur);
        }
      }
    } else {
      pre.push(cur);
    }
    return pre;
  }, []);
}

export function composeOperation(preOperation: IOperation, curOperation: IOperation): { operation: IOperation | null; isComposed: boolean } {
  if (
    curOperation.actions.length === preOperation.actions.length &&
    curOperation.actions.length === 1 &&
    preOperation.actions[0].p.length === curOperation.actions[0].p.length
  ) {
    // 过滤 od、oi 或者 li、ld 一样的 action
    const actions = jot.compose(curOperation.actions, preOperation.actions).filter((action: any) => {
      if (action.n === OTActionName.ObjectReplace) {
        return !isEqual(action.od, action.oi);
      }
      if (action.n === OTActionName.ListReplace) {
        return !isEqual(action.ld, action.li);
      }
      return true;
    });
    // 合并后 actions 不超过原 actions 长度
    if (actions.length <= curOperation.actions.length) {
      let operation: IOperation | null = null;
      if (actions.length > 0) {
        operation = { ...curOperation, actions };
      }
      return { operation, isComposed: true };
    }
  }
  return { operation: curOperation, isComposed: false };
}
