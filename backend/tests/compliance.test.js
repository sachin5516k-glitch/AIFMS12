const db = require('./utils/db');
const { checkCompliance } = require('../src/compliance/compliance.engine');
const Sales = require('../src/sales/salesModel');
const Inventory = require('../src/inventory/inventoryModel');
const Attendance = require('../src/attendance/attendanceModel');

beforeAll(async () => await db.connect());
afterEach(async () => await db.clear());
afterAll(async () => await db.close());

describe('Compliance Engine', () => {

    it('should report violation if no sales submitted', async () => {
        // Submit only Inventory and Attendance
        await Inventory.create({ outletId: 'outlet_c_1', items: [] });
        await Attendance.create({ userId: 'u1', outletId: 'outlet_c_1', checkInTime: new Date(), photoUrl: 'url' });

        const violations = await checkCompliance('outlet_c_1');

        expect(violations).toContain('Missing Daily Sales Report');
        expect(violations).not.toContain('Missing Inventory Update');
    });

    it('should pass if all metrics present', async () => {
        await Sales.create({ outletId: 'outlet_ok', amount: 100, paymentMode: 'Cash' });
        await Inventory.create({ outletId: 'outlet_ok', items: [] });
        await Attendance.create({ userId: 'u1', outletId: 'outlet_ok', checkInTime: new Date(), photoUrl: 'url' });

        const violations = await checkCompliance('outlet_ok');

        expect(violations).toHaveLength(0);
    });
});
