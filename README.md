import { BranchCountryCodeGuard } from './branch-country-code.guard';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { UserPreferenceKey } from '@shared/model/storage-keys.enum';
import * as utils from '@shared/utils/string.util';

describe('BranchCountryCodeGuard (Jest)', () => {
  let guard: BranchCountryCodeGuard;
  let mockRouter: jest.Mocked<Router>;

  const mockRoute = {
    paramMap: {
      get: jest.fn().mockImplementation((key: string) => {
        if (key === 'eventId') return 'E123';
        return null;
      })
    }
  } as unknown as ActivatedRouteSnapshot;

  const mockState = {} as RouterStateSnapshot;

  beforeEach(() => {
    mockRouter = {
      navigate: jest.fn()
    } as unknown as jest.Mocked<Router>;

    guard = new BranchCountryCodeGuard(mockRouter);

    jest.spyOn(utils, 'getSessionStorageValue').mockImplementation((key: UserPreferenceKey) => {
      if (key === UserPreferenceKey.BRANCH_CODES) return 'BR001';
      if (key === UserPreferenceKey.COUNTRY_CODES) return 'IN';
      return '';
    });

    jest.spyOn(utils, 'getBranchCodeFromId').mockReturnValue(['BR001']);
    jest.spyOn(utils, 'getCountryCodeFromId').mockReturnValue(['IN']);
  });

  it('should return true when branch and country codes are valid', () => {
    const result = guard.canActivateChild(mockRoute, mockState);
    expect(result).toBe(true);
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should return false and navigate when branch or country codes are invalid', () => {
    (utils.getBranchCodeFromId as jest.Mock).mockReturnValue(['BR999']); // force mismatch
    const result = guard.canActivateChild(mockRoute, mockState);
    expect(result).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});