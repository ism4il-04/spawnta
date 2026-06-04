import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommendationsFeed } from './recommendations-feed';

describe('RecommendationsFeed', () => {
  let component: RecommendationsFeed;
  let fixture: ComponentFixture<RecommendationsFeed>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecommendationsFeed],
    }).compileComponents();

    fixture = TestBed.createComponent(RecommendationsFeed);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
