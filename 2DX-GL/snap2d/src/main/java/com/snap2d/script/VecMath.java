/*
 *  Copyright (C) 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.script;

import java.util.HashMap;

import bg.x2d.geo.Vector2d;

/**
 * @author Brian Groenke
 *
 */
class VecMath {

    private VecMath() {

    }

    private static final HashMap <Character, VecOp> opMap;

    static {
        opMap = new HashMap <Character, VecOp>();
        for (int i = 0; i < 4; i++ ) {
            VecOp op = null;
            switch (i) {
            case 0:
                op = new AddOp();
                break;
            case 1:
                op = new SubOp();
                break;
            case 2:
                op = new MultOp();
                break;
            case 3:
                op = new DivOp();
            }
            opMap.put(MathRef.OPERATORS[i], op);
        }
    }

    protected static VecOp getOp(final char c) {

        return opMap.get(c);
    }

    protected interface VecOp {

        public Operand eval(Operand... args) throws MathParseException;

        public double argCount();
    }

    protected static class AddOp implements VecOp {

        /**
         * @throws MathParseException
         *
         */
        @Override
        public Vec2 eval(final Operand... args) throws MathParseException {

            if (args.length != argCount()) {
                throw (new MathParseException("invalid number of arguments for vector add"));
            }
            if (!args[0].isVector() || !args[1].isVector()) {
                throw (new MathParseException("invalid operands for vector add"));
            }
            Vector2d vec1 = ((Vec2) args[0]).getValue();
            Vector2d vec2 = ((Vec2) args[1]).getValue();
            return new Vec2(vec1.addNew(vec2));
        }

        /**
         *
         */
        @Override
        public double argCount() {

            return 2;
        }

    }

    protected static class SubOp implements VecOp {

        /**
         * @throws MathParseException
         *
         */
        @Override
        public Vec2 eval(final Operand... args) throws MathParseException {

            if (args.length != argCount()) {
                throw (new MathParseException("invalid number of arguments for vector subtract"));
            }
            if (!args[0].isVector() || !args[1].isVector()) {
                throw (new MathParseException("invalid operands for vector subtract"));
            }
            Vector2d vec1 = ((Vec2) args[0]).getValue();
            Vector2d vec2 = ((Vec2) args[1]).getValue();
            return new Vec2(vec1.subNew(vec2));
        }

        /**
         *
         */
        @Override
        public double argCount() {

            return 2;
        }
    }

    protected static class MultOp implements VecOp {

        /**
         *
         */
        @Override
        public Operand eval(final Operand... args) throws MathParseException {

            checkArgCount(this, args);
            if (!args[0].isVector() && !args[1].isVector()) {
                throw (new MathParseException("at least one argument must be a vector for mult op"));
            } else if (!args[1].isVector()) {
                return mult((Scalar) args[1], (Vec2) args[0]);
            } else if (!args[0].isVector()) {
                return mult((Scalar) args[0], (Vec2) args[1]);
            } else {
                return dot((Vec2) args[0], (Vec2) args[1]);
            }
        }

        private Vec2 mult(final Scalar s, final Vec2 v) {

            return new Vec2(v.vec.multNew(s.val));
        }

        private Scalar dot(final Vec2 v1, final Vec2 v2) {

            return new Scalar(v1.vec.dot(v2.vec));
        }

        /**
         *
         */
        @Override
        public double argCount() {

            return 2;
        }
    }

    protected static class DivOp implements VecOp {

        /**
         *
         */
        @Override
        public Operand eval(final Operand... args) throws MathParseException {

            checkArgCount(this, args);
            if (args[0].isVector() && args[1].isVector()) {
                throw (new MathParseException("vectors can only be divided by a scalar"));
            }
            Scalar s = (Scalar) ( (args[0].isVector()) ? args[1] : args[0]);
            Vec2 v = (Vec2) ( (args[0].isVector()) ? args[0] : args[1]);
            return new Vec2(v.vec.divNew(s.val));
        }

        /**
         *
         */
        @Override
        public double argCount() {

            return 2;
        }
    }

    private static void checkArgCount(final VecOp op, final Operand... args) throws MathParseException {

        if (args.length != op.argCount()) {
            throw (new MathParseException("mismatched number of arguments"));
        }
    }
}
