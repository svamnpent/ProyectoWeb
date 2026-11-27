import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SummaryPanel } from './summary-panel';

describe('SummaryPanel', () => {
  let component: SummaryPanel;
  let fixture: ComponentFixture<SummaryPanel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SummaryPanel],
    }).compileComponents();

    fixture = TestBed.createComponent(SummaryPanel);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
