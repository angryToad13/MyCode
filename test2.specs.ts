import { TestBed } from ‘@angular/core/testing’;
import { Router } from ‘@angular/router’;
import { ActivatedRouteSnapshot, RouterStateSnapshot } from ‘@angular/router’;
import { BranchCountryCodeGuard } from ‘./branch-country-code.guard’;
import { getBranchCodeFromId, getCountryCodeFromId } from ‘@shared/utils/string.util’;
import { getSessionStorageValue } from ‘@shared/utils’;
import { UserPreferenceKey } from ‘@shared/model/storage-keys.enum’;

// Mock the utility functions
jest.mock(’@shared/utils/string.util’, () => ({
getBranchCodeFromId: jest.fn(),
getCountryCodeFromId: jest.fn()
}));

jest.mock(’@shared/utils’, () => ({
getSessionStorageValue: jest.fn()
}));

describe(‘BranchCountryCodeGuard’, () => {
let guard: BranchCountryCodeGuard;
let router: Router;
let mockRouter: jest.Mocked<Router>;
let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
let mockRouterStateSnapshot: RouterStateSnapshot;

const mockGetBranchCodeFromId = getBranchCodeFromId as jest.MockedFunction<typeof getBranchCodeFromId>;
const mockGetCountryCodeFromId = getCountryCodeFromId as jest.MockedFunction<typeof getCountryCodeFromId>;
const mockGetSessionStorageValue = getSessionStorageValue as jest.MockedFunction<typeof getSessionStorageValue>;

beforeEach(() => {
const routerSpy = {
navigate: jest.fn()
};

```
TestBed.configureTestingModule({
  providers: [
    BranchCountryCodeGuard,
    { provide: Router, useValue: routerSpy }
  ]
});

guard = TestBed.inject(BranchCountryCodeGuard);
mockRouter = TestBed.inject(Router) as jest.Mocked<Router>;

// Create mock route snapshot
mockActivatedRouteSnapshot = {
  paramMap: {
    get: jest.fn()
  }
} as any;

// Create mock router state snapshot
mockRouterStateSnapshot = {} as RouterStateSnapshot;
```

});

afterEach(() => {
jest.clearAllMocks();
});

describe(‘canActivateChild’, () => {
beforeEach(() => {
// Setup default mock return values
(mockActivatedRouteSnapshot.paramMap.get as jest.Mock)
.mockReturnValueOnce(‘event123’) // eventId
.mockReturnValueOnce(‘branch001’) // localBranchCode  
.mockReturnValueOnce(‘country001’); // localCountryCode

```
  mockGetSessionStorageValue
    .mockReturnValueOnce('branch001') // BRANCH_CODES
    .mockReturnValueOnce('country001'); // COUNTRY_CODES
});

it('should be created', () => {
  expect(guard).toBeTruthy();
});

it('should return true when eventId is null', () => {
  (mockActivatedRouteSnapshot.paramMap.get as jest.Mock)
    .mockReturnValueOnce(null); // eventId is null

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(true);
  expect(mockRouter.navigate).not.toHaveBeenCalled();
});

it('should return true when hasValidBranchCountryCode returns true', () => {
  // Mock the utility functions to return valid codes
  mockGetBranchCodeFromId.mockReturnValue('branch001');
  mockGetCountryCodeFromId.mockReturnValue('country001');

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(true);
  expect(mockRouter.navigate).not.toHaveBeenCalled();
});

it('should navigate to "/" and return false when hasValidBranchCountryCode returns false', () => {
  // Mock the utility functions to return invalid codes
  mockGetBranchCodeFromId.mockReturnValue('invalid-branch');
  mockGetCountryCodeFromId.mockReturnValue('invalid-country');

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(false);
  expect(mockRouter.navigate).toHaveBeenCalledWith({ commands: ['/'] });
});

it('should handle missing eventId parameter', () => {
  (mockActivatedRouteSnapshot.paramMap.get as jest.Mock)
    .mockImplementation((param: string) => {
      if (param === 'eventId') return null;
      if (param === 'localBranchCode') return 'branch001';
      if (param === 'localCountryCode') return 'country001';
      return null;
    });

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(true);
  expect(mockRouter.navigate).not.toHaveBeenCalled();
});

it('should handle missing localBranchCode parameter', () => {
  (mockActivatedRouteSnapshot.paramMap.get as jest.Mock)
    .mockImplementation((param: string) => {
      if (param === 'eventId') return 'event123';
      if (param === 'localBranchCode') return null;
      if (param === 'localCountryCode') return 'country001';
      return null;
    });

  // Mock getBranchCodeFromId to return -1 (not found)
  mockGetBranchCodeFromId.mockReturnValue(-1);
  mockGetCountryCodeFromId.mockReturnValue('country001');

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(false);
  expect(mockRouter.navigate).toHaveBeenCalledWith({ commands: ['/'] });
});

it('should handle missing localCountryCode parameter', () => {
  (mockActivatedRouteSnapshot.paramMap.get as jest.Mock)
    .mockImplementation((param: string) => {
      if (param === 'eventId') return 'event123';
      if (param === 'localBranchCode') return 'branch001';
      if (param === 'localCountryCode') return null;
      return null;
    });

  // Mock getCountryCodeFromId to return -1 (not found)
  mockGetBranchCodeFromId.mockReturnValue('branch001');
  mockGetCountryCodeFromId.mockReturnValue(-1);

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(false);
  expect(mockRouter.navigate).toHaveBeenCalledWith({ commands: ['/'] });
});

it('should call getBranchCodeFromId and getCountryCodeFromId with correct parameters', () => {
  mockGetBranchCodeFromId.mockReturnValue('branch001');
  mockGetCountryCodeFromId.mockReturnValue('country001');

  guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(mockGetBranchCodeFromId).toHaveBeenCalledWith('event123');
  expect(mockGetCountryCodeFromId).toHaveBeenCalledWith('event123');
});

it('should call getSessionStorageValue with correct keys', () => {
  mockGetBranchCodeFromId.mockReturnValue('branch001');
  mockGetCountryCodeFromId.mockReturnValue('country001');

  guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(mockGetSessionStorageValue).toHaveBeenCalledWith(UserPreferenceKey.BRANCH_CODES);
  expect(mockGetSessionStorageValue).toHaveBeenCalledWith(UserPreferenceKey.COUNTRY_CODES);
});

it('should handle when session storage returns null/undefined', () => {
  mockGetBranchCodeFromId.mockReturnValue('branch001');
  mockGetCountryCodeFromId.mockReturnValue('country001');
  mockGetSessionStorageValue.mockReturnValue(null);

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(false);
  expect(mockRouter.navigate).toHaveBeenCalledWith({ commands: ['/'] });
});

it('should validate branch code correctly when branch code is not found in session storage', () => {
  mockGetBranchCodeFromId.mockReturnValue('invalid-branch');
  mockGetCountryCodeFromId.mockReturnValue('country001');
  
  // Mock session storage to return valid country but not contain the branch
  mockGetSessionStorageValue
    .mockReturnValueOnce('different-branch') // BRANCH_CODES
    .mockReturnValueOnce('country001'); // COUNTRY_CODES

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(false);
  expect(mockRouter.navigate).toHaveBeenCalledWith({ commands: ['/'] });
});

it('should validate country code correctly when country code is not found in session storage', () => {
  mockGetBranchCodeFromId.mockReturnValue('branch001');
  mockGetCountryCodeFromId.mockReturnValue('invalid-country');
  
  // Mock session storage to return valid branch but not contain the country
  mockGetSessionStorageValue
    .mockReturnValueOnce('branch001') // BRANCH_CODES
    .mockReturnValueOnce('different-country'); // COUNTRY_CODES

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(false);
  expect(mockRouter.navigate).toHaveBeenCalledWith({ commands: ['/'] });
});
```

});

describe(‘hasValidBranchCountryCode private method behavior’, () => {
it(‘should return false when getBranchCodeFromId returns -1’, () => {
mockGetBranchCodeFromId.mockReturnValue(-1);
mockGetCountryCodeFromId.mockReturnValue(‘country001’);
mockGetSessionStorageValue
.mockReturnValueOnce(‘branch001’)
.mockReturnValueOnce(‘country001’);

```
  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(false);
});

it('should return false when getCountryCodeFromId returns -1', () => {
  mockGetBranchCodeFromId.mockReturnValue('branch001');
  mockGetCountryCodeFromId.mockReturnValue(-1);
  mockGetSessionStorageValue
    .mockReturnValueOnce('branch001')
    .mockReturnValueOnce('country001');

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(false);
});

it('should return false when both getBranchCodeFromId and getCountryCodeFromId return -1', () => {
  mockGetBranchCodeFromId.mockReturnValue(-1);
  mockGetCountryCodeFromId.mockReturnValue(-1);
  mockGetSessionStorageValue
    .mockReturnValueOnce('branch001')
    .mockReturnValueOnce('country001');

  const result = guard.canActivateChild(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

  expect(result).toBe(false);
});
```

});
});