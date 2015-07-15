describe('SummationRuleTest', function () {
	it('creating and verifying 1', function () {
		var rule = new SummationRule({
			id: 1,
			type: "summation",
			optionIds: [1, 5, 23, 54, 75, 99, 120],
			lowerBound: 0,
			upperBound: 5
		});

		var map = new Map();
		map.put(1, 1);
		map.put(23, 2);

		var v = rule.verify(map);
		expect(v).toEqual(true);
	});


	it('creating and verifying 2', function () {
		var rule = new SummationRule({
			id: 1,
			type: "summation",
			optionIds: [1, 5, 23, 54, 75, 99, 120],
			lowerBound: 0,
			upperBound: 5
		});

		var map = new Map();
		map.put(1, 2);
		map.put(23, 4);

		var v = rule.verify(map);
		expect(v).toEqual(false);
	});

	it('containing option', function () {
		var rule = new SummationRule({
			id: 1,
			type: "summation",
			optionIds: [1, 5, 23, 54, 75, 99, 120],
			lowerBound: 0,
			upperBound: 5
		});

		expect(rule.containsOption(1)).toEqual(true);
		expect(rule.containsOption(54)).toEqual(true);
		expect(rule.containsOption("1")).toEqual(false);
		expect(rule.containsOption(7)).toEqual(false);
	});
});


describe('CumulationRuleTest', function () {
	it('creating and verifying 1', function () {
		var rule = new CumulationRule({
			id: 1,
			type: "cumulation",
			optionIds: [1, 5, 23, 54, 75, 99, 120],
			lowerBound: 0,
			upperBound: 2
		});

		var map = new Map();
		map.put(1, 1);
		map.put(23, 2);

		var v = rule.verify(map);
		expect(v).toEqual(true);
	});


	it('creating and verifying (upperBound) 2', function () {
		var rule = new CumulationRule({
			id: 1,
			type: "cumulation",
			optionIds: [1, 5, 23, 54, 75, 99, 120],
			lowerBound: 0,
			upperBound: 2
		});

		var map = new Map();
		map.put(1, 2);
		map.put(23, 4);

		var v = rule.verify(map);
		expect(v).toEqual(false);
	});

	it('creating and verifying (lowerBound) 3', function () {
		var rule = new CumulationRule({
			id: 1,
			type: "cumulation",
			optionIds: [1, 5, 23, 54],
			lowerBound: 1,
			upperBound: 3
		});

		var map = new Map();
		map.put(1, 2);
		map.put(5, 2);
		map.put(23, 3);

		var v = rule.verify(map);
		expect(v).toEqual(false);
	});

	it('containing option', function () {
		var rule = new CumulationRule({
			id: 1,
			type: "cumulation",
			optionIds: [1, 5, 23, 54, 75, 99, 120],
			lowerBound: 0,
			upperBound: 5
		});

		expect(rule.containsOption(1)).toEqual(true);
		expect(rule.containsOption(54)).toEqual(true);
		expect(rule.containsOption("1")).toEqual(false);
		expect(rule.containsOption(7)).toEqual(false);
	});

});