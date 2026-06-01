import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivityRating } from './activity-rating';

describe('ActivityRating', () => {
  let component: ActivityRating;
  let fixture: ComponentFixture<ActivityRating>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActivityRating],
    }).compileComponents();

    fixture = TestBed.createComponent(ActivityRating);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
